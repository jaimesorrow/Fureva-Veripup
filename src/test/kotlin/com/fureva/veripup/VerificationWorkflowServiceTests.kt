package com.fureva.veripup

import com.fureva.veripup.integration.MockAkcVerificationProvider
import com.fureva.veripup.integration.MockClinicVerificationProvider
import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.OnboardingAgreementType
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.service.BreederOnboardingService
import com.fureva.veripup.service.VerificationService
import com.fureva.veripup.workflow.InMemoryBreederOnboardingRepository
import com.fureva.veripup.workflow.InMemoryBreederProfileRepository
import com.fureva.veripup.workflow.InMemoryVerificationReviewRepository
import com.fureva.veripup.workflow.VerificationReviewStatus
import com.fureva.veripup.workflow.VerificationWorkflowService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class VerificationWorkflowServiceTests {

    private fun workflow(): VerificationWorkflowService =
        VerificationWorkflowService(
            breederProfiles = InMemoryBreederProfileRepository(),
            onboardingSubmissions = InMemoryBreederOnboardingRepository(),
            verificationReviews = InMemoryVerificationReviewRepository(),
            onboardingService = BreederOnboardingService(),
            verificationService = VerificationService(
                clinicVerificationProvider = MockClinicVerificationProvider(),
                akcVerificationProvider = MockAkcVerificationProvider()
            )
        )

    private fun registerBreeder(workflow: VerificationWorkflowService, breederId: String = "b1") {
        workflow.registerBreeder(
            BreederProfile(
                id = breederId,
                name = "Test Breeder",
                stateCode = "AK",
                city = "Anchorage"
            )
        )
    }

    private fun completeOnboarding(breederId: String = "b1") = BreederOnboardingSubmission(
        breederId = breederId,
        governmentIdUploaded = true,
        photoHoldingGovernmentIdUploaded = true,
        vetRecordsUploaded = true,
        vetRecordsCoverBreedingDogs = true,
        acceptedAgreements = OnboardingAgreementType.entries.toSet(),
        signedAt = Instant.parse("2026-01-01T00:00:00Z")
    )

    private fun validVerification(breederId: String = "b1", akc: String? = "AKC-12345") = VerificationSubmission(
        breederId = breederId,
        governmentIdReadable = true,
        idMatchesSignup = true,
        firstLiveVideoPassedDeepfake = true,
        vetDocsUploaded = true,
        vetDocsMatchBreed = true,
        optionalAkcNumber = akc,
        secondLiveVideoPassedDeepfake = true,
        clinicPhone = "9075551234",
        roiSigned = true
    )

    @Test
    fun verificationRejectedWhenOnboardingMissing() {
        val workflow = workflow()
        registerBreeder(workflow)

        val outcome = workflow.submitVerification(validVerification())

        assertFalse(outcome.acceptedForAdminReview)
        assertEquals(VerificationReviewStatus.REJECTED, outcome.status)
        assertTrue(outcome.message.contains("Onboarding submission is required"))
    }

    @Test
    fun verificationRejectedWhenOnboardingIncomplete() {
        val workflow = workflow()
        registerBreeder(workflow)
        workflow.submitOnboarding(
            completeOnboarding().copy(
                vetRecordsUploaded = false,
                acceptedAgreements = setOf(OnboardingAgreementType.GOOD_BREEDING_INTENTIONS)
            )
        )

        val outcome = workflow.submitVerification(validVerification())

        assertFalse(outcome.acceptedForAdminReview)
        assertEquals(VerificationReviewStatus.REJECTED, outcome.status)
        assertTrue(outcome.message.contains("Onboarding is incomplete"))
    }

    @Test
    fun validVerificationMovesIntoAdminQueue() {
        val workflow = workflow()
        registerBreeder(workflow)
        workflow.submitOnboarding(completeOnboarding())

        val outcome = workflow.submitVerification(validVerification())

        assertTrue(outcome.acceptedForAdminReview)
        assertEquals(VerificationReviewStatus.READY_FOR_ADMIN_REVIEW, outcome.status)
        assertEquals(1, workflow.listVerificationQueue().size)
    }

    @Test
    fun adminApprovalMarksBreederVerified() {
        val workflow = workflow()
        registerBreeder(workflow)
        workflow.submitOnboarding(completeOnboarding())
        workflow.submitVerification(validVerification())
        val pendingRecord = workflow.listVerificationQueue().single()

        val reviewed = workflow.reviewVerification(pendingRecord.id, approved = true, reviewNotes = "Looks good")

        assertEquals(VerificationReviewStatus.APPROVED, reviewed.status)
        assertTrue(workflow.getBreeder("b1")!!.verifiedStatus)
    }

    @Test
    fun policyFailureSkipsAdminQueueAndKeepsBreederUnverified() {
        val workflow = workflow()
        registerBreeder(workflow)
        workflow.submitOnboarding(completeOnboarding())

        val outcome = workflow.submitVerification(validVerification(akc = "BAD-123"))

        assertFalse(outcome.acceptedForAdminReview)
        assertEquals(VerificationReviewStatus.REJECTED, outcome.status)
        assertTrue(workflow.listVerificationQueue().isEmpty())
        assertFalse(workflow.getBreeder("b1")!!.verifiedStatus)
    }

    @Test
    fun duplicatePendingVerificationIsRejectedUntilReviewed() {
        val workflow = workflow()
        registerBreeder(workflow)
        workflow.submitOnboarding(completeOnboarding())
        workflow.submitVerification(validVerification())

        val error = assertFailsWith<IllegalArgumentException> {
            workflow.submitVerification(validVerification(akc = null))
        }

        assertTrue(error.message!!.contains("already pending"))
        assertEquals(1, workflow.listVerificationRecords().size)
    }

    @Test
    fun rejectedSubmissionHistoryIsPreservedAcrossResubmission() {
        val workflow = workflow()
        registerBreeder(workflow)
        workflow.submitOnboarding(completeOnboarding())

        workflow.submitVerification(validVerification(akc = "BAD-123"))
        workflow.submitVerification(validVerification(akc = "AKC-77777"))

        assertEquals(2, workflow.listVerificationRecords().size)
        assertEquals(VerificationReviewStatus.READY_FOR_ADMIN_REVIEW, workflow.listVerificationQueue().single().status)
    }

    @Test
    fun breederIdMustContainAnAlphanumericCharacter() {
        val workflow = workflow()

        val error = assertFailsWith<IllegalArgumentException> {
            workflow.registerBreeder(
                BreederProfile(
                    id = "---",
                    name = "Invalid",
                    stateCode = "AK",
                    city = "Anchorage"
                )
            )
        }

        assertTrue(error.message!!.contains("Breeder ID"))
    }
}
