package com.fureva.veripup.service

import com.fureva.veripup.model.AppealStatus
import com.fureva.veripup.model.BreederProfile
import java.time.Duration
import java.time.Instant

/**
 * Handles platform enforcement actions against breeders who violate the terms
 * of service, and manages the subsequent appeal process.
 *
 * Enforcement actions are typically triggered by admin review of flagged
 * off-platform or fraudulent activity. Deactivated breeders automatically
 * enter a [AppealStatus.PENDING] state so they may contest the decision.
 */
class EnforcementService {
    /**
     * Deactivates a breeder's account in response to an off-platform policy
     * violation and immediately opens an appeal window.
     *
     * The returned [BreederProfile] has:
     * - [BreederProfile.active] set to `false`
     * - [BreederProfile.deactivatedAt] set to [now]
     * - [BreederProfile.appealStatus] set to [AppealStatus.PENDING]
     * - [BreederProfile.appealOpenedAt] set to [now]
     *
     * This function is pure — it returns a modified copy and does not mutate
     * the original profile or persist anything.
     *
     * @param profile The breeder profile to deactivate.
     * @param now The current timestamp, used to record when the action occurred.
     * @return An updated [BreederProfile] reflecting the enforcement action.
     */
    fun markOffPlatformViolation(profile: BreederProfile, now: Instant): BreederProfile {
        return profile.copy(
            active = false,
            deactivatedAt = now,
            appealStatus = AppealStatus.PENDING,
            appealOpenedAt = now
        )
    }

    /**
     * Records the outcome of an admin appeal review, either reinstating or
     * permanently deactivating the breeder.
     *
     * - If [approved] is `true`: sets [AppealStatus.APPROVED] and reactivates the account.
     * - If [approved] is `false`: sets [AppealStatus.REJECTED] and keeps the account inactive.
     *
     * @param profile The breeder profile with a pending appeal.
     * @param approved `true` to reinstate the breeder; `false` to uphold the deactivation.
     * @return An updated [BreederProfile] reflecting the appeal outcome.
     */
    fun resolveAppeal(profile: BreederProfile, approved: Boolean): BreederProfile {
        val newStatus = if (approved) AppealStatus.APPROVED else AppealStatus.REJECTED
        return profile.copy(
            appealStatus = newStatus,
            active = approved
        )
    }

    /**
     * Returns `true` if an open appeal has been awaiting an admin decision for
     * more than 90 days, signalling that it requires escalation.
     *
     * If [BreederProfile.appealOpenedAt] is `null` (no appeal is open), this
     * function returns `false` immediately.
     *
     * @param profile The breeder profile to check.
     * @param now The current timestamp to measure the appeal age against.
     * @return `true` if the appeal is older than 90 days; `false` otherwise.
     */
    fun isAdminDecisionOverdue(profile: BreederProfile, now: Instant): Boolean {
        val appealOpened = profile.appealOpenedAt ?: return false
        return Duration.between(appealOpened, now).toDays() > 90
    }
}
