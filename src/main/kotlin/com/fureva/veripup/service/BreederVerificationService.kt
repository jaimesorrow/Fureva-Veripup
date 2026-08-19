package com.fureva.veripup.service

import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.BreederVerificationStatus
import com.fureva.veripup.model.User
import com.fureva.veripup.model.VerificationDocumentType
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.model.VerificationSubmissionStatus
import java.time.Instant

/**
 * Verification is entirely admin-driven: breeders can never self-assign a badge.
 * A breeder can create a litter once their own email and phone are verified,
 * independent of whether their breeder badge has been approved.
 */
class BreederVerificationService {

    fun canCreateLitter(user: User): Boolean = user.emailVerified && user.phoneVerified

    /**
     * Applies an admin decision to a pending submission and returns the updated submission
     * plus the breeder profile's next verification status. Approving an IDENTITY submission
     * advances an UNVERIFIED breeder to IDENTITY_VERIFIED; approving any other submission type
     * advances an IDENTITY_VERIFIED breeder to DOCUMENTS_VERIFIED. TRUSTED_BREEDER is a
     * separate, explicit admin promotion and is never reached automatically.
     */
    fun reviewSubmission(
        submission: VerificationSubmission,
        profile: BreederProfile,
        approve: Boolean,
        reviewerAdminId: String,
        now: Instant,
        rejectionReason: String? = null
    ): Pair<VerificationSubmission, BreederProfile> {
        require(submission.status == VerificationSubmissionStatus.PENDING) {
            "Only pending submissions can be reviewed"
        }
        require(submission.breederProfileId == profile.id) { "Submission does not belong to this breeder profile" }

        if (!approve) {
            require(!rejectionReason.isNullOrBlank()) { "A rejection reason is required" }
            val rejected = submission.copy(
                status = VerificationSubmissionStatus.REJECTED,
                reviewedBy = reviewerAdminId,
                reviewedAt = now,
                rejectionReason = rejectionReason
            )
            return rejected to profile
        }

        val approved = submission.copy(
            status = VerificationSubmissionStatus.APPROVED,
            reviewedBy = reviewerAdminId,
            reviewedAt = now,
            rejectionReason = null
        )

        val nextStatus = when {
            profile.verificationStatus == BreederVerificationStatus.UNVERIFIED &&
                submission.verificationType == VerificationDocumentType.IDENTITY ->
                BreederVerificationStatus.IDENTITY_VERIFIED

            profile.verificationStatus == BreederVerificationStatus.IDENTITY_VERIFIED ->
                BreederVerificationStatus.DOCUMENTS_VERIFIED

            else -> profile.verificationStatus
        }

        return approved to profile.copy(verificationStatus = nextStatus, updatedAt = now)
    }

    /** TRUSTED_BREEDER requires an explicit admin action and only after documents are verified. */
    fun promoteToTrustedBreeder(profile: BreederProfile, now: Instant): BreederProfile {
        require(profile.verificationStatus == BreederVerificationStatus.DOCUMENTS_VERIFIED) {
            "Only documents-verified breeders can be promoted to trusted breeder"
        }
        return profile.copy(verificationStatus = BreederVerificationStatus.TRUSTED_BREEDER, updatedAt = now)
    }

    fun suspend(profile: BreederProfile, now: Instant): BreederProfile =
        profile.copy(verificationStatus = BreederVerificationStatus.SUSPENDED, updatedAt = now)

    fun canClaimUsdaApproved(profile: BreederProfile): Boolean = profile.usdaLicenseVerified

    fun isBadgeVisible(status: BreederVerificationStatus): Boolean =
        status != BreederVerificationStatus.UNVERIFIED
}
