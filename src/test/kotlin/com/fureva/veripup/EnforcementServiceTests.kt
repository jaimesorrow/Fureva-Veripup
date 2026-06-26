package com.fureva.veripup

import com.fureva.veripup.model.AppealStatus
import com.fureva.veripup.model.BreederProfile
import com.fureva.veripup.service.EnforcementService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EnforcementServiceTests {

    private val service = EnforcementService()
    private val now = Instant.parse("2026-01-01T00:00:00Z")

    private fun activeProfile(id: String = "b1") =
        BreederProfile(id = id, name = "Test Breeder", stateCode = "AK", city = "Anchorage", active = true)

    // ── markOffPlatformViolation ──────────────────────────────────────────────

    @Test
    fun violationSetsActiveToFalse() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        assertFalse(flagged.active)
    }

    @Test
    fun violationRecordsDeactivatedAt() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        assertEquals(now, flagged.deactivatedAt)
    }

    @Test
    fun violationSetsPendingAppealStatus() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        assertEquals(AppealStatus.PENDING, flagged.appealStatus)
    }

    @Test
    fun violationRecordsAppealOpenedAt() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        assertEquals(now, flagged.appealOpenedAt)
    }

    @Test
    fun violationDoesNotMutateOriginalProfile() {
        val original = activeProfile()
        service.markOffPlatformViolation(original, now)
        assertTrue(original.active)
        assertEquals(AppealStatus.NONE, original.appealStatus)
    }

    // ── resolveAppeal ─────────────────────────────────────────────────────────

    @Test
    fun resolveAppealApprovedReactivatesBreeder() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        val resolved = service.resolveAppeal(flagged, approved = true)
        assertTrue(resolved.active)
        assertEquals(AppealStatus.APPROVED, resolved.appealStatus)
    }

    @Test
    fun resolveAppealRejectedKeepsBreederInactive() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        val resolved = service.resolveAppeal(flagged, approved = false)
        assertFalse(resolved.active)
        assertEquals(AppealStatus.REJECTED, resolved.appealStatus)
    }

    @Test
    fun resolveAppealDoesNotMutateFlaggedProfile() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        service.resolveAppeal(flagged, approved = true)
        assertEquals(AppealStatus.PENDING, flagged.appealStatus)
    }

    // ── isAdminDecisionOverdue ─────────────────────────────────────────────────

    @Test
    fun decisionNotOverdueOnExactlyDay90() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        val exactly90Days = now.plusSeconds(90L * 24 * 3600)
        assertFalse(service.isAdminDecisionOverdue(flagged, exactly90Days))
    }

    @Test
    fun decisionNotOverdueBeforeDay90() {
        val flagged = service.markOffPlatformViolation(activeProfile(), now)
        val day89 = now.plusSeconds(89L * 24 * 3600)
        assertFalse(service.isAdminDecisionOverdue(flagged, day89))
    }

    @Test
    fun decisionNotOverdueWhenNoAppealOpened() {
        // Profile that was never flagged has no appealOpenedAt
        val profile = activeProfile().copy(appealOpenedAt = null)
        assertFalse(service.isAdminDecisionOverdue(profile, now.plusSeconds(200L * 24 * 3600)))
    }
}
