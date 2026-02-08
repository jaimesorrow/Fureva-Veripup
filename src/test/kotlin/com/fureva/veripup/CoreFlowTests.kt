package com.fureva.veripup

import com.fureva.veripup.integration.MockAkcVerificationProvider
import com.fureva.veripup.integration.MockClinicVerificationProvider
import com.fureva.veripup.integration.SmsGateway
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.LitterRecord
import com.fureva.veripup.model.SmsPreference
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.model.VerifiedEventType
import com.fureva.veripup.service.AlertsService
import com.fureva.veripup.service.EnforcementService
import com.fureva.veripup.service.InventoryService
import com.fureva.veripup.service.VerificationService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.time.Instant

class CoreFlowTests {
    @Test
    fun verificationGatingFailsOnBadAkc() {
        val service = VerificationService(MockClinicVerificationProvider(), MockAkcVerificationProvider())
        val approved = service.approve(
            VerificationSubmission(
                breederId = "b1",
                governmentIdReadable = true,
                idMatchesSignup = true,
                firstLiveVideoPassedDeepfake = true,
                vetDocsUploaded = true,
                vetDocsMatchBreed = true,
                optionalAkcNumber = "BAD-123",
                secondLiveVideoPassedDeepfake = true,
                clinicPhone = "9075551234",
                roiSigned = true
            )
        )

        assertFalse(approved)
    }

    @Test
    fun inventoryCapsDepositsAndAdoptions() {
        val inventory = InventoryService()
        val record = LitterRecord(
            breederId = "b1",
            expectedLitterCount = 3,
            dueDateConfirmed = true,
            verifiedByVet = true,
            listedAvailablePuppies = 5,
            reservedDeposits = 3,
            completedAdoptions = 1
        )

        assertFalse(inventory.canAcceptDeposit(record))
        assertTrue(inventory.canCompleteAdoption(record))
        assertEquals(2, inventory.cappedAvailability(record))
    }

    @Test
    fun offPlatformEnforcementAndAppealTiming() {
        val service = EnforcementService()
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val profile = BreederProfile(id = "b1", name = "A", stateCode = "AK", city = "Anchorage")
        val flagged = service.markOffPlatformViolation(profile, now)

        assertFalse(flagged.active)
        assertTrue(service.isAdminDecisionOverdue(flagged, now.plusSeconds(91 * 24 * 3600L)))
    }

    @Test
    fun alertsOnlySendForOptInAndVerifiedEvents() {
        val sent = mutableListOf<String>()
        val gateway = object : SmsGateway {
            override fun send(to: String, message: String) {
                sent += "$to|$message"
            }
        }
        val alerts = AlertsService(gateway)

        val preference = SmsPreference("u1", "+19075551234", optedInVerifiedAlerts = true)
        assertTrue(alerts.canTriggerEvent(VerifiedEventType.NEW_PUPS_POSTED, breederVerified = true))
        alerts.sendVerifiedUpdate(preference, "New puppies listed")

        assertEquals(1, sent.size)
        assertTrue(sent.first().contains("Verified Update:"))
    }
}
