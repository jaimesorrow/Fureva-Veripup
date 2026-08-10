package com.fureva.veripup.workflow

import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.VerificationSubmission
import java.time.Instant

enum class VerificationReviewStatus {
    READY_FOR_ADMIN_REVIEW,
    APPROVED,
    REJECTED
}

data class OnboardingStatus(
    val breederId: String,
    val readyForVerification: Boolean,
    val missingRequirements: List<String>
)

data class VerificationReviewRecord(
    val id: String,
    val breederId: String,
    val submission: VerificationSubmission,
    val policyApproved: Boolean,
    val status: VerificationReviewStatus,
    val submittedAt: Instant,
    val reviewedAt: Instant? = null,
    val reviewNotes: String? = null
)

data class VerificationSubmissionOutcome(
    val breederId: String,
    val acceptedForAdminReview: Boolean,
    val status: VerificationReviewStatus,
    val message: String
)

data class BreederWorkflowSnapshot(
    val profile: BreederProfile,
    val onboardingSubmission: BreederOnboardingSubmission? = null,
    val onboardingStatus: OnboardingStatus? = null,
    val verificationRecord: VerificationReviewRecord? = null
)
