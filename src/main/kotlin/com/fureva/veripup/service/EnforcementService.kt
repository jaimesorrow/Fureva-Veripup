package com.fureva.veripup.service

import com.fureva.veripup.model.AppealStatus
import com.fureva.veripup.model.BreederProfile
import java.time.Duration
import java.time.Instant

class EnforcementService {
    fun markOffPlatformViolation(profile: BreederProfile, now: Instant): BreederProfile {
        return profile.copy(
            active = false,
            deactivatedAt = now,
            appealStatus = AppealStatus.PENDING,
            appealOpenedAt = now
        )
    }

    fun resolveAppeal(profile: BreederProfile, approved: Boolean): BreederProfile {
        val newStatus = if (approved) AppealStatus.APPROVED else AppealStatus.REJECTED
        return profile.copy(
            appealStatus = newStatus,
            active = approved
        )
    }

    fun isAdminDecisionOverdue(profile: BreederProfile, now: Instant): Boolean {
        val appealOpened = profile.appealOpenedAt ?: return false
        return Duration.between(appealOpened, now).toDays() > 90
    }
}
