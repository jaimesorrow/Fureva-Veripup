package com.fureva.veripup

import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.OnboardingAgreementType
import com.fureva.veripup.service.BreederOnboardingService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BreederOnboardingServiceTests {

    private val service = BreederOnboardingService()
    private val allAgreements = OnboardingAgreementType.entries.toSet()
    private val signedAt = Instant.parse("2026-03-01T00:00:00Z")

    private fun completeSubmission() = BreederOnboardingSubmission(
        breederId = "b1",
        governmentIdUploaded = true,
        photoHoldingGovernmentIdUploaded = true,
        vetRecordsUploaded = true,
        vetRecordsCoverBreedingDogs = true,
        acceptedAgreements = allAgreements,
        signedAt = signedAt
    )

    // ── isReadyForVerification ─────────────────────────────────────────────────

    @Test
    fun notReadyWhenGovernmentIdMissing() {
        val s = completeSubmission().copy(governmentIdUploaded = false)
        assertFalse(service.isReadyForVerification(s))
    }

    @Test
    fun notReadyWhenPhotoHoldingIdMissing() {
        val s = completeSubmission().copy(photoHoldingGovernmentIdUploaded = false)
        assertFalse(service.isReadyForVerification(s))
    }

    @Test
    fun notReadyWhenVetRecordsNotUploaded() {
        val s = completeSubmission().copy(vetRecordsUploaded = false)
        assertFalse(service.isReadyForVerification(s))
    }

    @Test
    fun notReadyWhenVetRecordsDoNotCoverBreedingDogs() {
        val s = completeSubmission().copy(vetRecordsCoverBreedingDogs = false)
        assertFalse(service.isReadyForVerification(s))
    }

    @Test
    fun notReadyWhenSignatureIsMissing() {
        val s = completeSubmission().copy(signedAt = null)
        assertFalse(service.isReadyForVerification(s))
    }

    @Test
    fun notReadyWhenAgreementsAreEmpty() {
        val s = completeSubmission().copy(acceptedAgreements = emptySet())
        assertFalse(service.isReadyForVerification(s))
    }

    @Test
    fun notReadyWhenOnlyOneAgreementAccepted() {
        val s = completeSubmission().copy(
            acceptedAgreements = setOf(OnboardingAgreementType.GOOD_BREEDING_INTENTIONS)
        )
        assertFalse(service.isReadyForVerification(s))
    }

    // ── missingRequirements ────────────────────────────────────────────────────

    @Test
    fun missingRequirementsIsEmptyForCompleteSubmission() {
        assertTrue(service.missingRequirements(completeSubmission()).isEmpty())
    }

    @Test
    fun missingRequirementsListsGovernmentIdWhenAbsent() {
        val missing = service.missingRequirements(completeSubmission().copy(governmentIdUploaded = false))
        assertTrue(missing.any { it.contains("Government ID", ignoreCase = true) })
    }

    @Test
    fun missingRequirementsListsPhotoHoldingIdWhenAbsent() {
        val missing = service.missingRequirements(
            completeSubmission().copy(photoHoldingGovernmentIdUploaded = false)
        )
        assertTrue(missing.any { it.contains("Photo holding", ignoreCase = true) })
    }

    @Test
    fun missingRequirementsListsVetRecordsUploadWhenAbsent() {
        val missing = service.missingRequirements(completeSubmission().copy(vetRecordsUploaded = false))
        assertTrue(missing.any { it.contains("Vet records upload", ignoreCase = true) })
    }

    @Test
    fun missingRequirementsListsVetRecordsBreedingDogsWhenAbsent() {
        val missing = service.missingRequirements(completeSubmission().copy(vetRecordsCoverBreedingDogs = false))
        assertTrue(missing.any { it.contains("breeding dogs", ignoreCase = true) })
    }

    @Test
    fun missingRequirementsListsSignatureWhenAbsent() {
        val missing = service.missingRequirements(completeSubmission().copy(signedAt = null))
        assertTrue(missing.any { it.contains("signature", ignoreCase = true) })
    }

    @Test
    fun missingRequirementsListsEachMissingAgreement() {
        val presentAgreements = setOf(OnboardingAgreementType.GOOD_BREEDING_INTENTIONS)
        val s = completeSubmission().copy(acceptedAgreements = presentAgreements)
        val missing = service.missingRequirements(s)
        val expectedMissingAgreements = allAgreements - presentAgreements
        for (agreement in expectedMissingAgreements) {
            assertTrue(missing.any { it.contains(agreement.name) },
                "Expected missing requirement for $agreement")
        }
    }

    @Test
    fun missingRequirementsCountWhenAllAgreementsMissing() {
        val s = completeSubmission().copy(acceptedAgreements = emptySet())
        // 5 agreement entries should all appear in the missing list
        val missing = service.missingRequirements(s)
        assertEquals(5, missing.filter { it.startsWith("Agreement:") }.size)
    }

    @Test
    fun missingRequirementsCountWhenEverythingMissing() {
        val s = BreederOnboardingSubmission(
            breederId = "b2",
            governmentIdUploaded = false,
            photoHoldingGovernmentIdUploaded = false,
            vetRecordsUploaded = false,
            vetRecordsCoverBreedingDogs = false,
            acceptedAgreements = emptySet(),
            signedAt = null
        )
        // 5 non-agreement fields + 5 agreements = 10 missing items
        assertEquals(10, service.missingRequirements(s).size)
    }
}
