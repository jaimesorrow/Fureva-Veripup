package com.fureva.veripup

import com.fureva.veripup.integration.MockPaymentProvider
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.model.BreederVerificationStatus
import com.fureva.veripup.model.CampaignStage
import com.fureva.veripup.model.Dog
import com.fureva.veripup.model.DogType
import com.fureva.veripup.model.Litter
import com.fureva.veripup.model.LitterStatus
import com.fureva.veripup.model.ModerationActionType
import com.fureva.veripup.model.PaymentStatus
import com.fureva.veripup.model.Report
import com.fureva.veripup.model.ReportReason
import com.fureva.veripup.model.ReportTargetType
import com.fureva.veripup.model.ReservationStatus
import com.fureva.veripup.model.Role
import com.fureva.veripup.model.User
import com.fureva.veripup.model.UserStatus
import com.fureva.veripup.model.VerificationDocumentType
import com.fureva.veripup.model.VerificationSubmission
import com.fureva.veripup.service.ApplicationWaitlistService
import com.fureva.veripup.service.BreederVerificationService
import com.fureva.veripup.service.LitterCampaignService
import com.fureva.veripup.service.ModerationService
import com.fureva.veripup.service.ReservationService
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * End-to-end coverage of the breeder onboarding -> litter marketing -> buyer -> reporting
 * flows described in the product spec, using an Anchorage litter as the walking skeleton.
 */
class LitterMarketingFlowTests {

    private val now = Instant.parse("2026-05-01T00:00:00Z")

    @Test
    fun breederOnboardingLitterPublishingAndDepositFlow() {
        val verificationService = BreederVerificationService()
        val campaignService = LitterCampaignService()
        val reservationService = ReservationService(MockPaymentProvider())

        val breederUser = User(
            id = "u-breeder",
            email = "breeder@northernpaws.example",
            phone = "+19075551234",
            role = Role.BREEDER,
            status = UserStatus.ACTIVE,
            emailVerified = true,
            phoneVerified = true,
            createdAt = now,
            updatedAt = now
        )
        assertTrue(verificationService.canCreateLitter(breederUser))

        var profile = BreederProfile(
            id = "bp-northern-paws",
            userId = breederUser.id,
            kennelName = "Northern Paws",
            slug = "northern-paws",
            city = "Anchorage",
            region = "Anchorage",
            createdAt = now,
            updatedAt = now
        )

        val identitySubmission = VerificationSubmission(
            id = "vs1",
            breederProfileId = profile.id,
            verificationType = VerificationDocumentType.IDENTITY,
            documentUrl = "https://storage.example/id.pdf",
            createdAt = now
        )
        val (_, afterIdentity) = verificationService.reviewSubmission(
            identitySubmission, profile, approve = true, reviewerAdminId = "admin1", now = now
        )
        profile = afterIdentity
        assertEquals(BreederVerificationStatus.IDENTITY_VERIFIED, profile.verificationStatus)

        var litter = Litter(
            id = "l-anchorage-golden",
            breederProfileId = profile.id,
            title = "Anchorage Golden Retriever Litter",
            slug = "anchorage-golden-retriever-litter",
            breed = "Golden Retriever",
            city = "Anchorage",
            region = "Anchorage",
            campaignStage = CampaignStage.BORN,
            birthDate = LocalDate.parse("2026-03-01"),
            accuracyConfirmed = true,
            createdAt = now,
            updatedAt = now
        )
        assertTrue(campaignService.canPublish(litter))
        litter = campaignService.publish(litter, now)
        assertEquals(LitterStatus.PUBLISHED, litter.status)

        val puppy = Dog(
            id = "p1",
            breederProfileId = profile.id,
            litterId = litter.id,
            dogType = DogType.PUPPY,
            name = "Denali",
            dateOfBirth = litter.birthDate,
            breed = litter.breed,
            priceUsd = BigDecimal("1800"),
            hasPhoto = true,
            createdAt = now,
            updatedAt = now
        )
        val goHomeDay = litter.birthDate!!.plusWeeks(8)
        assertTrue(campaignService.canMarkAvailable(puppy, goHomeDay))

        val reservation = reservationService.createReservation(
            litterId = litter.id,
            puppyId = puppy.id,
            buyerUserId = "u-buyer",
            breederProfileId = profile.id,
            amount = BigDecimal("500"),
            refundPolicySummary = "Fully refundable until the litter is confirmed ready for homes.",
            refundPolicyAcknowledged = true,
            now = now
        )
        val paid = reservationService.applyWebhookEvent(reservation, "evt_1", PaymentStatus.PAID, now)
        assertEquals(ReservationStatus.CONFIRMED, paid.reservationStatus)
    }

    @Test
    fun buyerApplicationAndWaitlistFlow() {
        val waitlistService = ApplicationWaitlistService()
        val entries = waitlistService.activeEntriesInOrder(emptyList())
        assertTrue(entries.isEmpty())
        assertEquals(1, waitlistService.nextWaitlistPosition(emptyList()))
    }

    @Test
    fun reportingFlowWritesAuditLog() {
        val moderationService = ModerationService()
        val report = Report(
            id = "rep1",
            reporterUserId = "u-buyer",
            targetType = ReportTargetType.LISTING,
            targetId = "l-anchorage-golden",
            reason = ReportReason.MISREPRESENTATION,
            createdAt = now
        )
        val assigned = moderationService.assign(report, "admin1")
        val (resolved, entry) = moderationService.resolve(assigned, "admin1", "Listing corrected by breeder", now)
        assertEquals(ModerationActionType.RESOLVE_REPORT, entry.actionType)
        assertTrue(moderationService.auditLogEntries.isNotEmpty())
        assertEquals("Listing corrected by breeder", resolved.resolutionNotes)
    }
}
