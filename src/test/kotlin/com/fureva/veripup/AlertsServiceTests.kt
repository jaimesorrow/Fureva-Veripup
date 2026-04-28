package com.fureva.veripup

import com.fureva.veripup.integration.SmsGateway
import com.fureva.veripup.model.SmsPreference
import com.fureva.veripup.model.VerifiedEventType
import com.fureva.veripup.service.AlertsService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AlertsServiceTests {

    private fun recordingGateway(sent: MutableList<Pair<String, String>>): SmsGateway =
        object : SmsGateway {
            override fun send(to: String, message: String) {
                sent += to to message
            }
        }

    private fun service(sent: MutableList<Pair<String, String>> = mutableListOf()) =
        AlertsService(recordingGateway(sent))

    // ── sendVerifiedUpdate ────────────────────────────────────────────────────

    @Test
    fun sendVerifiedUpdateDoesNotSendWhenUserOptedOut() {
        val sent = mutableListOf<Pair<String, String>>()
        val alerts = service(sent)
        val preference = SmsPreference("u1", "+19075551234", optedInVerifiedAlerts = false)
        alerts.sendVerifiedUpdate(preference, "Puppies available")
        assertTrue(sent.isEmpty())
    }

    @Test
    fun sendVerifiedUpdateSendsToCorrectPhoneNumber() {
        val sent = mutableListOf<Pair<String, String>>()
        val alerts = service(sent)
        val phone = "+19075559999"
        val preference = SmsPreference("u2", phone, optedInVerifiedAlerts = true)
        alerts.sendVerifiedUpdate(preference, "Hello")
        assertEquals(phone, sent.single().first)
    }

    @Test
    fun sendVerifiedUpdatePrefixesMessageWithVerifiedUpdate() {
        val sent = mutableListOf<Pair<String, String>>()
        val alerts = service(sent)
        val preference = SmsPreference("u3", "+19075551111", optedInVerifiedAlerts = true)
        alerts.sendVerifiedUpdate(preference, "Litter confirmed")
        assertTrue(sent.single().second.startsWith("Verified Update:"))
    }

    @Test
    fun sendVerifiedUpdateIncludesProvidedTextInMessage() {
        val sent = mutableListOf<Pair<String, String>>()
        val alerts = service(sent)
        val preference = SmsPreference("u4", "+19075552222", optedInVerifiedAlerts = true)
        val text = "New litter expected March 1"
        alerts.sendVerifiedUpdate(preference, text)
        assertTrue(sent.single().second.contains(text))
    }

    @Test
    fun sendVerifiedUpdateSendsExactlyOnce() {
        val sent = mutableListOf<Pair<String, String>>()
        val alerts = service(sent)
        val preference = SmsPreference("u5", "+19075553333", optedInVerifiedAlerts = true)
        alerts.sendVerifiedUpdate(preference, "Test")
        assertEquals(1, sent.size)
    }

    // ── canTriggerEvent ───────────────────────────────────────────────────────

    @Test
    fun cannotTriggerAnyEventWhenBreederNotVerified() {
        val alerts = service()
        for (eventType in VerifiedEventType.entries) {
            assertFalse(alerts.canTriggerEvent(eventType, breederVerified = false),
                "Expected false for $eventType when breeder is not verified")
        }
    }

    @Test
    fun canTriggerNewPupsPostedWhenVerified() {
        assertTrue(service().canTriggerEvent(VerifiedEventType.NEW_PUPS_POSTED, breederVerified = true))
    }

    @Test
    fun canTriggerAvailabilityChangedWhenVerified() {
        assertTrue(service().canTriggerEvent(VerifiedEventType.AVAILABILITY_CHANGED, breederVerified = true))
    }

    @Test
    fun canTriggerUpcomingLitterPolicyAllowedWhenVerified() {
        assertTrue(service().canTriggerEvent(VerifiedEventType.UPCOMING_LITTER_POLICY_ALLOWED, breederVerified = true))
    }

    @Test
    fun canTriggerVetConfirmedDepositUnlockWhenVerified() {
        assertTrue(service().canTriggerEvent(VerifiedEventType.VET_CONFIRMED_DEPOSIT_UNLOCK, breederVerified = true))
    }
}
