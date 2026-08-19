package com.fureva.veripup.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

// ── Users & roles ────────────────────────────────────────────────────────────

enum class Role { BUYER, BREEDER, TRANSPORT_PROVIDER, ADMIN, PARTNER_BUSINESS }
enum class UserStatus { ACTIVE, SUSPENDED, DELETED }

data class User(
    val id: String,
    val email: String,
    val phone: String? = null,
    val role: Role,
    val status: UserStatus = UserStatus.ACTIVE,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ── Breeders & verification ─────────────────────────────────────────────────

enum class BreederVerificationStatus {
    UNVERIFIED,
    IDENTITY_VERIFIED,
    DOCUMENTS_VERIFIED,
    TRUSTED_BREEDER,
    REJECTED,
    SUSPENDED
}

data class BreederProfile(
    val id: String,
    val userId: String,
    val kennelName: String,
    val slug: String,
    val bio: String? = null,
    val city: String,
    val region: String? = null,
    val serviceAreas: List<String> = emptyList(),
    val breeds: List<String> = emptyList(),
    val websiteUrl: String? = null,
    val instagramUrl: String? = null,
    val facebookUrl: String? = null,
    val verificationStatus: BreederVerificationStatus = BreederVerificationStatus.UNVERIFIED,
    val verificationNotes: String? = null,
    val publicPhoneEnabled: Boolean = false,
    val usdaLicenseVerified: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

enum class VerificationDocumentType {
    IDENTITY,
    PARENT_RECORDS,
    HEALTH_TESTING,
    VETERINARY_REFERENCE,
    USDA_LICENSE
}

enum class VerificationSubmissionStatus { PENDING, APPROVED, REJECTED, RESUBMISSION_REQUESTED }

data class VerificationSubmission(
    val id: String,
    val breederProfileId: String,
    val verificationType: VerificationDocumentType,
    val documentUrl: String,
    val documentMetadata: Map<String, String> = emptyMap(),
    val status: VerificationSubmissionStatus = VerificationSubmissionStatus.PENDING,
    val reviewedBy: String? = null,
    val reviewedAt: Instant? = null,
    val rejectionReason: String? = null,
    val createdAt: Instant
)

// ── Dogs (parents & puppies) ────────────────────────────────────────────────

enum class DogType { DAM, SIRE, PUPPY, OTHER }
enum class PuppyStatus { AVAILABLE, PENDING, RESERVED, PLACED, NOT_AVAILABLE }

data class Dog(
    val id: String,
    val breederProfileId: String,
    val litterId: String? = null,
    val dogType: DogType,
    val name: String,
    val sex: String? = null,
    val breed: String? = null,
    val color: String? = null,
    val dateOfBirth: LocalDate? = null,
    val description: String? = null,
    val temperament: String? = null,
    val weightLbs: BigDecimal? = null,
    val status: PuppyStatus? = null,
    val publicVisible: Boolean = true,
    val hasPhoto: Boolean = false,
    val priceUsd: BigDecimal? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ── Litters ──────────────────────────────────────────────────────────────────

enum class CampaignStage {
    PLANNED,
    CONFIRMED_PREGNANCY,
    BORN,
    TAKING_APPLICATIONS,
    RESERVATION_OPEN,
    READY_FOR_HOMES,
    CLOSED
}

enum class LitterStatus { DRAFT, PUBLISHED, HIDDEN }

data class Litter(
    val id: String,
    val breederProfileId: String,
    val damId: String? = null,
    val sireId: String? = null,
    val title: String,
    val slug: String,
    val breed: String,
    val breedSecondary: String? = null,
    val city: String,
    val region: String? = null,
    val description: String? = null,
    val campaignStage: CampaignStage,
    val expectedBirthDate: LocalDate? = null,
    val birthDate: LocalDate? = null,
    val expectedGoHomeDate: LocalDate? = null,
    val confirmedGoHomeDate: LocalDate? = null,
    val totalPuppies: Int? = null,
    val availableCount: Int = 0,
    val priceMin: BigDecimal? = null,
    val priceMax: BigDecimal? = null,
    val currency: String = "USD",
    val waitlistOpen: Boolean = true,
    val applicationsOpen: Boolean = true,
    val transportAvailable: Boolean = false,
    val publishedAt: Instant? = null,
    val status: LitterStatus = LitterStatus.DRAFT,
    val accuracyConfirmed: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ── Litter media & updates ──────────────────────────────────────────────────

enum class MediaType { IMAGE, VIDEO }

data class LitterMedia(
    val id: String,
    val litterId: String,
    val mediaType: MediaType,
    val storageKey: String,
    val thumbnailKey: String? = null,
    val altText: String? = null,
    val sortOrder: Int = 0,
    val isCover: Boolean = false,
    val createdAt: Instant
)

enum class MilestoneType {
    BIRTH,
    WEEKLY_UPDATE,
    DEVELOPMENT,
    VET_VISIT,
    VACCINATION,
    PERSONALITY,
    RESERVATION_OPEN,
    GO_HOME,
    OTHER
}

enum class UpdateVisibility { PUBLIC, APPLICANTS_ONLY }

data class LitterUpdate(
    val id: String,
    val litterId: String,
    val title: String,
    val body: String? = null,
    val media: List<String> = emptyList(),
    val milestoneType: MilestoneType? = null,
    val visibility: UpdateVisibility = UpdateVisibility.PUBLIC,
    val publishedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ── Health records ───────────────────────────────────────────────────────────

enum class HealthRecordType {
    VET_EXAM,
    VACCINATION,
    DEWORMING,
    GENETIC_TEST,
    HIP_ELBOW_TEST,
    EYE_EXAM,
    MICROCHIP,
    HEALTH_GUARANTEE,
    OTHER
}

enum class HealthRecordVisibility { FULL, SUMMARY, PRIVATE }
enum class HealthVerificationStatus { UNVERIFIED, VERIFIED }

data class HealthRecord(
    val id: String,
    val dogId: String? = null,
    val litterId: String? = null,
    val recordType: HealthRecordType,
    val providerName: String? = null,
    val recordDate: LocalDate? = null,
    val documentUrl: String? = null,
    val publicVisibility: HealthRecordVisibility = HealthRecordVisibility.SUMMARY,
    val verificationStatus: HealthVerificationStatus = HealthVerificationStatus.UNVERIFIED,
    val createdAt: Instant
)

// ── Applications & waitlist ──────────────────────────────────────────────────

enum class ApplicationStatus {
    SUBMITTED, UNDER_REVIEW, APPROVED, WAITLISTED, DECLINED, WITHDRAWN, MATCHED
}

data class Application(
    val id: String,
    val buyerUserId: String,
    val litterId: String,
    val preferredPuppyId: String? = null,
    val status: ApplicationStatus = ApplicationStatus.SUBMITTED,
    val householdDetails: Map<String, String> = emptyMap(),
    val housingDetails: Map<String, String> = emptyMap(),
    val dogExperience: Map<String, String> = emptyMap(),
    val preferenceDetails: Map<String, String> = emptyMap(),
    val submittedAt: Instant,
    val reviewedAt: Instant? = null,
    val reviewedBy: String? = null,
    val internalNotes: String? = null
)

enum class WaitlistStatus { ACTIVE, OFFERED, CONVERTED, REMOVED }

data class WaitlistEntry(
    val id: String,
    val litterId: String,
    val buyerUserId: String,
    val applicationId: String? = null,
    val position: Int,
    val status: WaitlistStatus = WaitlistStatus.ACTIVE,
    val preferredSex: String? = null,
    val preferredColor: String? = null,
    val budgetMax: BigDecimal? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ── Reservations & payments ─────────────────────────────────────────────────

enum class PaymentStatus { PENDING, PAID, REFUNDED, FAILED, DISPUTED }
enum class ReservationStatus { PENDING, CONFIRMED, CANCELLED, EXPIRED, FROZEN }

data class Reservation(
    val id: String,
    val litterId: String,
    val puppyId: String,
    val buyerUserId: String,
    val breederProfileId: String,
    val amount: BigDecimal,
    val currency: String = "USD",
    val refundPolicySummary: String,
    val refundPolicyAcknowledged: Boolean = false,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val reservationStatus: ReservationStatus = ReservationStatus.PENDING,
    val paymentProvider: String? = null,
    val providerPaymentId: String? = null,
    val lastProcessedEventId: String? = null,
    val expiresAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

// ── Messaging ────────────────────────────────────────────────────────────────

data class Conversation(
    val id: String,
    val litterId: String? = null,
    val participantUserIds: Set<String>,
    val createdAt: Instant
)

data class Message(
    val id: String,
    val conversationId: String,
    val senderUserId: String,
    val body: String,
    val attachmentUrl: String? = null,
    val createdAt: Instant,
    val readAt: Instant? = null
)

// ── Trust & moderation ───────────────────────────────────────────────────────

enum class ReportTargetType { LISTING, BREEDER, BUYER, MESSAGE, REVIEW }

enum class ReportReason {
    SCAM_CONCERN,
    SUSPECTED_STOLEN_PHOTOS,
    ANIMAL_WELFARE_CONCERN,
    MISREPRESENTATION,
    PAYMENT_ISSUE,
    HARASSMENT,
    OTHER
}

enum class ReportStatus { OPEN, IN_REVIEW, RESOLVED, DISMISSED }

data class Report(
    val id: String,
    val reporterUserId: String,
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: ReportReason,
    val description: String? = null,
    val evidenceUrls: List<String> = emptyList(),
    val status: ReportStatus = ReportStatus.OPEN,
    val assignedAdminId: String? = null,
    val resolutionNotes: String? = null,
    val createdAt: Instant,
    val resolvedAt: Instant? = null
)

enum class ModerationActionType {
    WARN, HIDE_LISTING, FREEZE_DEPOSIT, REQUEST_DOCUMENTATION, SUSPEND_ACCOUNT, RESOLVE_REPORT
}

data class AuditLogEntry(
    val id: String,
    val adminUserId: String,
    val actionType: ModerationActionType,
    val targetType: ReportTargetType,
    val targetId: String,
    val reason: String,
    val createdAt: Instant
)
