package com.fureva.veripup.workflow

import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.service.BreederOnboardingService
import com.fureva.veripup.service.VerificationService
import java.time.Clock
import java.time.Instant
import java.util.UUID

class WorkflowNotFoundException(message: String) : IllegalArgumentException(message)

private val breederIdPattern = Regex("[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?")

class VerificationWorkflowService(
    private val breederProfiles: BreederProfileRepository,
    private val onboardingSubmissions: BreederOnboardingRepository,
    private val verificationReviews: VerificationReviewRepository,
    private val onboardingService: BreederOnboardingService,
    private val verificationService: VerificationService,
    private val clock: Clock = Clock.systemUTC()
) {
    fun registerBreeder(profile: BreederProfile): BreederProfile {
        require(profile.id.matches(breederIdPattern)) {
            "Breeder ID may only contain letters, numbers, and hyphens."
        }
        breederProfiles.save(profile)
        return profile
    }

    fun listBreeders(): List<BreederProfile> = breederProfiles.findAll()

    fun getBreeder(id: String): BreederProfile? = breederProfiles.findById(id)

    fun submitOnboarding(submission: BreederOnboardingSubmission): OnboardingStatus {
        requireBreeder(submission.breederId)
        onboardingSubmissions.save(submission)
        return evaluateOnboarding(submission)
    }

    fun getOnboardingStatus(breederId: String): OnboardingStatus? =
        onboardingSubmissions.findByBreederId(breederId)?.let(::evaluateOnboarding)

    fun submitVerification(submission: VerificationSubmission): VerificationSubmissionOutcome {
        requireBreeder(submission.breederId)
        val onboarding = onboardingSubmissions.findByBreederId(submission.breederId)
            ?: return saveRejectedVerification(
                submission = submission,
                message = "Onboarding submission is required before verification."
            )

        val onboardingStatus = evaluateOnboarding(onboarding)
        if (!onboardingStatus.readyForVerification) {
            return saveRejectedVerification(
                submission = submission,
                message = "Onboarding is incomplete: ${onboardingStatus.missingRequirements.joinToString()}"
            )
        }
        val latestRecord = verificationReviews.findLatestByBreederId(submission.breederId)
        require(latestRecord?.status != VerificationReviewStatus.READY_FOR_ADMIN_REVIEW) {
            "A verification review is already pending for breeder '${submission.breederId}'."
        }

        val policyApproved = verificationService.approve(submission)
        val record = VerificationReviewRecord(
            id = UUID.randomUUID().toString(),
            breederId = submission.breederId,
            submission = submission,
            policyApproved = policyApproved,
            status = if (policyApproved) {
                VerificationReviewStatus.READY_FOR_ADMIN_REVIEW
            } else {
                VerificationReviewStatus.REJECTED
            },
            submittedAt = now(),
            reviewedAt = if (policyApproved) null else now(),
            reviewNotes = if (policyApproved) {
                "Queued for admin review."
            } else {
                "Verification policy checks failed."
            }
        )
        verificationReviews.save(record)

        return VerificationSubmissionOutcome(
            breederId = submission.breederId,
            acceptedForAdminReview = policyApproved,
            status = record.status,
            message = record.reviewNotes ?: ""
        )
    }

    fun listVerificationQueue(): List<VerificationReviewRecord> = verificationReviews.findPendingReview()

    fun listVerificationRecords(): List<VerificationReviewRecord> = verificationReviews.findAll()

    fun getWorkflowSnapshot(breederId: String): BreederWorkflowSnapshot? {
        val profile = breederProfiles.findById(breederId) ?: return null
        val onboardingSubmission = onboardingSubmissions.findByBreederId(breederId)
        return BreederWorkflowSnapshot(
            profile = profile,
            onboardingSubmission = onboardingSubmission,
            onboardingStatus = onboardingSubmission?.let(::evaluateOnboarding),
            verificationRecord = verificationReviews.findLatestByBreederId(breederId)
        )
    }

    fun reviewVerification(recordId: String, approved: Boolean, reviewNotes: String? = null): VerificationReviewRecord {
        val existing = verificationReviews.findByRecordId(recordId)
            ?: throw WorkflowNotFoundException("No verification record found for record '$recordId'.")
        if (existing.status != VerificationReviewStatus.READY_FOR_ADMIN_REVIEW) {
            throw IllegalArgumentException("Verification record '$recordId' is not pending admin review.")
        }

        val resolvedStatus = if (approved && existing.policyApproved) {
            VerificationReviewStatus.APPROVED
        } else {
            VerificationReviewStatus.REJECTED
        }

        val updated = existing.copy(
            status = resolvedStatus,
            reviewedAt = now(),
            reviewNotes = when {
                reviewNotes.isNullOrBlank() && resolvedStatus == VerificationReviewStatus.APPROVED ->
                    "Approved by admin."
                reviewNotes.isNullOrBlank() && approved && !existing.policyApproved ->
                    "Approval blocked because policy checks failed."
                reviewNotes.isNullOrBlank() ->
                    "Rejected by admin."
                else -> reviewNotes
            }
        )
        verificationReviews.save(updated)

        val breeder = requireBreeder(existing.breederId)
        breederProfiles.save(
            breeder.copy(
                verifiedStatus = breeder.verifiedStatus || updated.status == VerificationReviewStatus.APPROVED
            )
        )

        return updated
    }

    private fun saveRejectedVerification(
        submission: VerificationSubmission,
        message: String
    ): VerificationSubmissionOutcome {
        verificationReviews.save(
            VerificationReviewRecord(
                id = UUID.randomUUID().toString(),
                breederId = submission.breederId,
                submission = submission,
                policyApproved = false,
                status = VerificationReviewStatus.REJECTED,
                submittedAt = now(),
                reviewedAt = now(),
                reviewNotes = message
            )
        )
        return VerificationSubmissionOutcome(
            breederId = submission.breederId,
            acceptedForAdminReview = false,
            status = VerificationReviewStatus.REJECTED,
            message = message
        )
    }

    private fun evaluateOnboarding(submission: BreederOnboardingSubmission): OnboardingStatus =
        OnboardingStatus(
            breederId = submission.breederId,
            readyForVerification = onboardingService.isReadyForVerification(submission),
            missingRequirements = onboardingService.missingRequirements(submission)
        )

    private fun requireBreeder(breederId: String): BreederProfile =
        breederProfiles.findById(breederId)
            ?: throw WorkflowNotFoundException("Breeder '$breederId' has not been registered.")

    private fun now(): Instant = Instant.now(clock)
}
