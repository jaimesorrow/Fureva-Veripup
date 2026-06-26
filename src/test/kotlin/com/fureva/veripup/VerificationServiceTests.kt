package com.fureva.veripup

import com.fureva.veripup.integration.MockAkcVerificationProvider
import com.fureva.veripup.integration.MockClinicVerificationProvider
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.service.VerificationService
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VerificationServiceTests {

    private fun validSubmission(
        akcNumber: String? = null,
        clinicPhone: String = "9075551234"
    ) = VerificationSubmission(
        breederId = "b1",
        governmentIdReadable = true,
        idMatchesSignup = true,
        firstLiveVideoPassedDeepfake = true,
        vetDocsUploaded = true,
        vetDocsMatchBreed = true,
        optionalAkcNumber = akcNumber,
        secondLiveVideoPassedDeepfake = true,
        clinicPhone = clinicPhone,
        roiSigned = true
    )

    private fun service() = VerificationService(MockClinicVerificationProvider(), MockAkcVerificationProvider())

    // ── Happy paths ──────────────────────────────────────────────────────────

    @Test
    fun approveSucceedsWithNoAkcNumber() {
        assertTrue(service().approve(validSubmission(akcNumber = null)))
    }

    @Test
    fun approveSucceedsWithValidAkcNumber() {
        assertTrue(service().approve(validSubmission(akcNumber = "AKC-99999")))
    }

    // ── Required-field failures ───────────────────────────────────────────────

    @Test
    fun approveFailsWhenGovernmentIdNotReadable() {
        assertFalse(service().approve(validSubmission().copy(governmentIdReadable = false)))
    }

    @Test
    fun approveFailsWhenIdDoesNotMatchSignup() {
        assertFalse(service().approve(validSubmission().copy(idMatchesSignup = false)))
    }

    @Test
    fun approveFailsWhenFirstLiveVideoFails() {
        assertFalse(service().approve(validSubmission().copy(firstLiveVideoPassedDeepfake = false)))
    }

    @Test
    fun approveFailsWhenVetDocsNotUploaded() {
        assertFalse(service().approve(validSubmission().copy(vetDocsUploaded = false)))
    }

    @Test
    fun approveFailsWhenVetDocsDoNotMatchBreed() {
        assertFalse(service().approve(validSubmission().copy(vetDocsMatchBreed = false)))
    }

    @Test
    fun approveFailsWhenSecondLiveVideoFails() {
        assertFalse(service().approve(validSubmission().copy(secondLiveVideoPassedDeepfake = false)))
    }

    @Test
    fun approveFailsWhenRoiNotSigned() {
        assertFalse(service().approve(validSubmission().copy(roiSigned = false)))
    }

    @Test
    fun approveFailsWhenClinicPhoneTooShort() {
        // MockClinicVerificationProvider returns false for phone numbers shorter than 10 chars
        assertFalse(service().approve(validSubmission(clinicPhone = "123")))
    }

    @Test
    fun approveFailsWhenMultipleRequiredChecksFail() {
        val submission = validSubmission().copy(
            governmentIdReadable = false,
            roiSigned = false
        )
        assertFalse(service().approve(submission))
    }
}
