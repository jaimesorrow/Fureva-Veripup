package com.fureva.veripup.model

import java.time.Instant

enum class Role { BREEDER, USER, ADMIN }
enum class TrustBadge { VERIPUP_VERIFIED, LEGACY_BREEDER }
enum class AppealStatus { NONE, PENDING, APPROVED, REJECTED }

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

data class LitterRecord(
    val breederId: String,
    val expectedLitterCount: Int,
    val dueDateConfirmed: Boolean,
    val verifiedByVet: Boolean,
    val listedAvailablePuppies: Int = 0,
    val reservedDeposits: Int = 0,
    val completedAdoptions: Int = 0
)

data class SmsPreference(
    val userId: String,
    val phoneNumber: String,
    val optedInVerifiedAlerts: Boolean
)

enum class VerifiedEventType {
    NEW_PUPS_POSTED,
    AVAILABILITY_CHANGED,
    UPCOMING_LITTER_POLICY_ALLOWED,
    VET_CONFIRMED_DEPOSIT_UNLOCK
}
