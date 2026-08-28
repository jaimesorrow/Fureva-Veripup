package com.fureva.veripup

import com.fureva.veripup.integration.MockAkcVerificationProvider
import com.fureva.veripup.integration.MockClinicVerificationProvider
import com.fureva.veripup.integration.SmsGateway
import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.LitterRecord
import com.fureva.veripup.model.OnboardingAgreementType
import com.fureva.veripup.model.SmsPreference
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.model.VerifiedEventType
import com.fureva.veripup.service.AlertsService
import com.fureva.veripup.service.BreederOnboardingService
import com.fureva.veripup.service.EnforcementService
import com.fureva.veripup.service.InventoryService
import com.fureva.veripup.service.VerificationService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Integration-style tests that exercise the core business flows end-to-end
 * using in-memory mock providers.
 *
 * Each test corresponds to one key user or admin journey:
 * - Breeder verification gating (AKC number validation)
 * - Breeder onboarding completeness checks
 * - Litter inventory deposit / adoption caps
 * - Enforcement deactivation and appeal overdue detection
 * - SMS alert opt-in and verified-event gating
 */
class CoreFlowTests {
    /**
     * Verifies that a verification submission is rejected when an invalid AKC
     * member number is supplied. All other required fields are valid; only the
     * AKC number (`"BAD-123"`) fails the [MockAkcVerificationProvider] check.
     */
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

    /**
     * Confirms that a fully completed [BreederOnboardingSubmission] — with all
     * five agreements accepted, both ID documents uploaded, vet records uploaded
     * and covering breeding dogs, and a valid signature timestamp — is marked
     * ready for verification with no missing requirements.
     */
    @Test
    fun onboardingRequiresIdentityVetRecordsAndAllAgreements() {
        val service = BreederOnboardingService()
        val submission = BreederOnboardingSubmission(
            breederId = "b1",
            governmentIdUploaded = true,
            photoHoldingGovernmentIdUploaded = true,
            vetRecordsUploaded = true,
            vetRecordsCoverBreedingDogs = true,
            acceptedAgreements = OnboardingAgreementType.entries.toSet(),
            signedAt = Instant.parse("2026-01-01T00:00:00Z")
        )

        assertTrue(service.isReadyForVerification(submission))
        assertTrue(service.missingRequirements(submission).isEmpty())
    }

    /**
     * Confirms that [BreederOnboardingService.missingRequirements] returns the
     * correct count of unmet requirements (8) when a submission is missing the
     * photo ID upload, vet records, the onboarding signature, and four of the
     * five required agreements.
     */
    @Test
    fun onboardingReturnsMissingRequirementsWhenIncomplete() {
        val service = BreederOnboardingService()
        val submission = BreederOnboardingSubmission(
            breederId = "b2",
            governmentIdUploaded = true,
            photoHoldingGovernmentIdUploaded = false,
            vetRecordsUploaded = false,
            vetRecordsCoverBreedingDogs = false,
            acceptedAgreements = setOf(OnboardingAgreementType.GOOD_BREEDING_INTENTIONS),
            signedAt = null
        )

        assertFalse(service.isReadyForVerification(submission))
        assertEquals(8, service.missingRequirements(submission).size)
    }

    /**
     * Verifies [InventoryService] inventory rules for a litter with 3 expected
     * puppies, 3 reserved deposits, and 1 completed adoption:
     * - No further deposits may be accepted (all remaining slots are deposited).
     * - An adoption can still be completed (deposits > completedAdoptions).
     * - Public availability is capped at 2 (3 expected − 1 adopted), even
     *   though the breeder listed 5.
     */
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

    /**
     * Verifies the enforcement lifecycle:
     * 1. [EnforcementService.markOffPlatformViolation] deactivates the account
     *    immediately.
     * 2. [EnforcementService.isAdminDecisionOverdue] returns `true` when the
     *    appeal has been open for more than 90 days (tested at 91 days).
     */
    @Test
    fun offPlatformEnforcementAndAppealTiming() {
        val service = EnforcementService()
        val now = Instant.parse("2026-01-01T00:00:00Z")
        val profile = BreederProfile(id = "b1", name = "A", stateCode = "AK", city = "Anchorage")
        val flagged = service.markOffPlatformViolation(profile, now)

        assertFalse(flagged.active)
        assertTrue(service.isAdminDecisionOverdue(flagged, now.plusSeconds(91 * 24 * 3600L)))
    }

    /**
     * Verifies the two SMS alert guards:
     * 1. [AlertsService.canTriggerEvent] returns `true` for [VerifiedEventType.NEW_PUPS_POSTED]
     *    when the breeder is verified.
     * 2. [AlertsService.sendVerifiedUpdate] delivers exactly one SMS (prefixed
     *    with `"Verified Update:"`) when the user has opted in.
     */
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
