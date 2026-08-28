package com.fureva.veripup.model

import java.time.Instant

/** Roles that a platform user can hold. */
enum class Role {
    /** A dog breeder listing puppies on the platform. */
    BREEDER,
    /** A buyer browsing and adopting puppies. */
    USER,
    /** A platform administrator managing verification and enforcement. */
    ADMIN
}

/**
 * Trust badge granted to a breeder after passing the VeriPup verification
 * process or being recognised as a legacy member.
 */
enum class TrustBadge {
    /** Breeder has passed the full VeriPup verification pipeline. */
    VERIPUP_VERIFIED,
    /** Breeder was grandfathered in as a trusted legacy member. */
    LEGACY_BREEDER
}

/**
 * Lifecycle state of a breeder's appeal against an enforcement action.
 */
enum class AppealStatus {
    /** No appeal has been filed. */
    NONE,
    /** Appeal has been submitted and is awaiting admin review. */
    PENDING,
    /** Admin reviewed the appeal and reinstated the breeder. */
    APPROVED,
    /** Admin reviewed the appeal and upheld the deactivation. */
    REJECTED
}

/**
 * Core profile record for a breeder on the platform.
 *
 * @property id Unique breeder identifier.
 * @property name Display name of the breeder or kennel.
 * @property stateCode Two-letter US state code (e.g. "AK").
 * @property city City where the breeder is located.
 * @property verifiedStatus Whether the breeder has passed full VeriPup verification.
 * @property legacyStatus Whether the breeder holds legacy-member standing.
 * @property active Whether the breeder's account is currently active.
 * @property deactivatedAt Timestamp of the most recent deactivation, if applicable.
 * @property appealStatus Current status of any open enforcement appeal.
 * @property appealOpenedAt Timestamp when the active appeal was first filed.
 */
data class BreederProfile(
    val id: String,
    val name: String,
    val stateCode: String,
    val city: String,
    val verifiedStatus: Boolean = false,
    val legacyStatus: Boolean = false,
    val active: Boolean = true,
    val deactivatedAt: Instant? = null,
    val appealStatus: AppealStatus = AppealStatus.NONE,
    val appealOpenedAt: Instant? = null
)

/**
 * All evidence collected during the admin-reviewed verification pipeline for
 * a breeder seeking [TrustBadge.VERIPUP_VERIFIED] status.
 *
 * Every required field must evaluate to `true` before [com.fureva.veripup.service.VerificationService.approve]
 * will return `true`. [optionalAkcNumber], when provided, is cross-checked
 * against the AKC registry.
 *
 * @property breederId ID of the breeder being verified.
 * @property governmentIdReadable Government-issued ID document is legible in the upload.
 * @property idMatchesSignup Name on the ID matches the account sign-up details.
 * @property firstLiveVideoPassedDeepfake First liveness video passed deepfake detection.
 * @property vetDocsUploaded Veterinary documentation has been uploaded.
 * @property vetDocsMatchBreed Uploaded vet docs correspond to the declared breed.
 * @property optionalAkcNumber Optional AKC member number to be validated against the registry.
 * @property secondLiveVideoPassedDeepfake Second liveness video passed deepfake detection.
 * @property clinicPhone Phone number of the veterinary clinic, used for existence check.
 * @property roiSigned Breeder has signed the Release of Information agreement.
 */
data class VerificationSubmission(
    val breederId: String,
    val governmentIdReadable: Boolean,
    val idMatchesSignup: Boolean,
    val firstLiveVideoPassedDeepfake: Boolean,
    val vetDocsUploaded: Boolean,
    val vetDocsMatchBreed: Boolean,
    val optionalAkcNumber: String? = null,
    val secondLiveVideoPassedDeepfake: Boolean,
    val clinicPhone: String,
    val roiSigned: Boolean
)

/**
 * Tracks the inventory state of a single litter from announcement through
 * completed adoptions.
 *
 * Used by [com.fureva.veripup.service.InventoryService] to enforce deposit
 * and adoption caps and to derive accurate public availability counts.
 *
 * @property breederId ID of the breeder who owns this litter.
 * @property expectedLitterCount Total number of puppies expected in the litter.
 * @property dueDateConfirmed Whether the vet has confirmed the due date.
 * @property verifiedByVet Whether a vet has verified the pregnancy/litter.
 * @property listedAvailablePuppies Number of puppies the breeder has listed as available.
 * @property reservedDeposits Number of puppies with a paid deposit held against them.
 * @property completedAdoptions Number of puppies that have been fully adopted.
 */
data class LitterRecord(
    val breederId: String,
    val expectedLitterCount: Int,
    val dueDateConfirmed: Boolean,
    val verifiedByVet: Boolean,
    val listedAvailablePuppies: Int = 0,
    val reservedDeposits: Int = 0,
    val completedAdoptions: Int = 0
)

/**
 * Stores a user's SMS notification preferences for verified breeder updates.
 *
 * @property userId ID of the user who owns this preference record.
 * @property phoneNumber E.164 or standard US phone number to send SMS to.
 * @property optedInVerifiedAlerts Whether the user has opted in to receiving
 *   "Verified Update" SMS alerts from breeders they follow.
 */
data class SmsPreference(
    val userId: String,
    val phoneNumber: String,
    val optedInVerifiedAlerts: Boolean
)

/**
 * Categories of platform events that can trigger a "Verified Update" SMS alert.
 * Alerts are only sent when the originating breeder is verified and the
 * recipient has opted in via [SmsPreference.optedInVerifiedAlerts].
 */
enum class VerifiedEventType {
    /** Breeder has posted a new litter or individual puppies for adoption. */
    NEW_PUPS_POSTED,
    /** Availability count on an existing listing has changed. */
    AVAILABILITY_CHANGED,
    /** An upcoming litter has been made visible under the platform's advance-listing policy. */
    UPCOMING_LITTER_POLICY_ALLOWED,
    /** Vet confirmation received, unlocking deposit acceptance for a litter. */
    VET_CONFIRMED_DEPOSIT_UNLOCK
}

/**
 * Legal and ethical agreements that every breeder must accept during
 * the onboarding flow before verification can begin.
 */
enum class OnboardingAgreementType {
    /** Commitment to breed responsibly and in the best interest of the animals. */
    GOOD_BREEDING_INTENTIONS,
    /** Commitment to maintain humane living conditions and veterinary care. */
    HUMANE_CARE_STANDARDS,
    /** Commitment to provide accurate, truthful listings and disclosures. */
    ACCURATE_LISTING_DISCLOSURES,
    /** Acceptance of platform payment terms, fee structure, and adoption rules. */
    PLATFORM_PAYMENT_AND_ADOPTION_RULES,
    /** Acceptance of the app's Terms of Service and Privacy Policy. */
    APP_TERMS_AND_PRIVACY_POLICY
}

/**
 * Collects all documents, photos, and signed agreements submitted by a breeder
 * during the initial onboarding step.
 *
 * Validated by [com.fureva.veripup.service.BreederOnboardingService] to determine
 * whether the breeder may proceed to the full verification pipeline.
 *
 * @property breederId ID of the breeder completing onboarding.
 * @property governmentIdUploaded Whether a government-issued ID document was uploaded.
 * @property photoHoldingGovernmentIdUploaded Whether a selfie holding the government ID was uploaded.
 * @property vetRecordsUploaded Whether at least one veterinary record was uploaded.
 * @property vetRecordsCoverBreedingDogs Whether the uploaded records cover the breeder's breeding dogs.
 * @property acceptedAgreements Set of [OnboardingAgreementType] items the breeder has accepted.
 * @property signedAt Timestamp at which the breeder signed the onboarding declaration, or `null` if not yet signed.
 */
data class BreederOnboardingSubmission(
    val breederId: String,
    val governmentIdUploaded: Boolean,
    val photoHoldingGovernmentIdUploaded: Boolean,
    val vetRecordsUploaded: Boolean,
    val vetRecordsCoverBreedingDogs: Boolean,
    val acceptedAgreements: Set<OnboardingAgreementType>,
    val signedAt: Instant?
)
