package com.fureva.veripup

import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.BreederVerificationStatus
import com.fureva.veripup.model.Role
import com.fureva.veripup.model.User
import com.fureva.veripup.model.UserStatus
import com.fureva.veripup.model.VerificationDocumentType
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.model.VerificationSubmissionStatus
import com.fureva.veripup.service.BreederVerificationService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BreederVerificationServiceTests {

    private val service = BreederVerificationService()
    private val now = Instant.parse("2026-01-01T00:00:00Z")

    private fun user(emailVerified: Boolean, phoneVerified: Boolean) = User(
        id = "u1",
        email = "breeder@example.com",
        phone = "+19075551234",
        role = Role.BREEDER,
        status = UserStatus.ACTIVE,
        emailVerified = emailVerified,
        phoneVerified = phoneVerified,
        createdAt = now,
        updatedAt = now
    )

    private fun profile(status: BreederVerificationStatus = BreederVerificationStatus.UNVERIFIED) = BreederProfile(
        id = "bp1",
        userId = "u1",
        kennelName = "Northern Paws",
        slug = "northern-paws",
        city = "Anchorage",
        region = "Anchorage",
        verificationStatus = status,
        createdAt = now,
        updatedAt = now
    )

    private fun submission(
        type: VerificationDocumentType = VerificationDocumentType.IDENTITY,
        breederProfileId: String = "bp1"
    ) = VerificationSubmission(
        id = "vs1",
        breederProfileId = breederProfileId,
        verificationType = type,
        documentUrl = "https://storage.example/doc.pdf",
        createdAt = now
    )

    // ── canCreateLitter ──────────────────────────────────────────────────────

    @Test
    fun canCreateLitterRequiresBothEmailAndPhoneVerified() {
        assertTrue(service.canCreateLitter(user(emailVerified = true, phoneVerified = true)))
    }

    @Test
    fun cannotCreateLitterWithoutEmailVerification() {
        assertFalse(service.canCreateLitter(user(emailVerified = false, phoneVerified = true)))
    }

    @Test
    fun cannotCreateLitterWithoutPhoneVerification() {
        assertFalse(service.canCreateLitter(user(emailVerified = true, phoneVerified = false)))
    }

    // ── reviewSubmission ─────────────────────────────────────────────────────

    @Test
    fun approvingIdentitySubmissionAdvancesUnverifiedBreederToIdentityVerified() {
        val (_, updatedProfile) = service.reviewSubmission(
            submission(), profile(), approve = true, reviewerAdminId = "admin1", now = now
        )
        assertEquals(BreederVerificationStatus.IDENTITY_VERIFIED, updatedProfile.verificationStatus)
    }

    @Test
    fun approvingSubmissionForIdentityVerifiedBreederAdvancesToDocumentsVerified() {
        val (_, updatedProfile) = service.reviewSubmission(
            submission(type = VerificationDocumentType.VETERINARY_REFERENCE),
            profile(status = BreederVerificationStatus.IDENTITY_VERIFIED),
            approve = true,
            reviewerAdminId = "admin1",
            now = now
        )
        assertEquals(BreederVerificationStatus.DOCUMENTS_VERIFIED, updatedProfile.verificationStatus)
    }

    @Test
    fun rejectingSubmissionRequiresReason() {
        assertFailsWith<IllegalArgumentException> {
            service.reviewSubmission(submission(), profile(), approve = false, reviewerAdminId = "admin1", now = now)
        }
    }

    @Test
    fun rejectingSubmissionSetsRejectedStatusAndReason() {
        val (updatedSubmission, updatedProfile) = service.reviewSubmission(
            submission(), profile(), approve = false, reviewerAdminId = "admin1", now = now,
            rejectionReason = "Document illegible"
        )
        assertEquals(VerificationSubmissionStatus.REJECTED, updatedSubmission.status)
        assertEquals("Document illegible", updatedSubmission.rejectionReason)
        assertEquals(BreederVerificationStatus.UNVERIFIED, updatedProfile.verificationStatus)
    }

    @Test
    fun reviewingAlreadyReviewedSubmissionFails() {
        val alreadyApproved = submission().copy(status = VerificationSubmissionStatus.APPROVED)
        assertFailsWith<IllegalArgumentException> {
            service.reviewSubmission(alreadyApproved, profile(), approve = true, reviewerAdminId = "admin1", now = now)
        }
    }

    @Test
    fun reviewingSubmissionForWrongProfileFails() {
        assertFailsWith<IllegalArgumentException> {
            service.reviewSubmission(
                submission(breederProfileId = "other"), profile(), approve = true, reviewerAdminId = "admin1", now = now
            )
        }
    }

    // ── promoteToTrustedBreeder ──────────────────────────────────────────────

    @Test
    fun promoteToTrustedBreederRequiresDocumentsVerifiedFirst() {
        assertFailsWith<IllegalArgumentException> {
            service.promoteToTrustedBreeder(profile(status = BreederVerificationStatus.IDENTITY_VERIFIED), now)
        }
    }

    @Test
    fun promoteToTrustedBreederSucceedsFromDocumentsVerified() {
        val promoted = service.promoteToTrustedBreeder(profile(status = BreederVerificationStatus.DOCUMENTS_VERIFIED), now)
        assertEquals(BreederVerificationStatus.TRUSTED_BREEDER, promoted.verificationStatus)
    }

    // ── suspend / badge visibility / USDA claim ─────────────────────────────

    @Test
    fun suspendSetsSuspendedStatus() {
        val suspended = service.suspend(profile(status = BreederVerificationStatus.TRUSTED_BREEDER), now)
        assertEquals(BreederVerificationStatus.SUSPENDED, suspended.verificationStatus)
    }

    @Test
    fun badgeIsHiddenWhenUnverified() {
        assertFalse(service.isBadgeVisible(BreederVerificationStatus.UNVERIFIED))
    }

    @Test
    fun badgeIsVisibleOnceAnyVerificationLevelReached() {
        assertTrue(service.isBadgeVisible(BreederVerificationStatus.IDENTITY_VERIFIED))
    }

    @Test
    fun cannotClaimUsdaApprovedWithoutVerifiedLicense() {
        assertFalse(service.canClaimUsdaApproved(profile()))
    }

    @Test
    fun canClaimUsdaApprovedWithVerifiedLicense() {
        assertTrue(service.canClaimUsdaApproved(profile().copy(usdaLicenseVerified = true)))
    }
}
