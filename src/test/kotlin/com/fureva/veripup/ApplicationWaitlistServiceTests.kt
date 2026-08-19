package com.fureva.veripup

import com.fureva.veripup.model.Application
import com.fureva.veripup.model.ApplicationStatus
import com.fureva.veripup.model.WaitlistEntry
import com.fureva.veripup.model.WaitlistStatus
import com.fureva.veripup.service.ApplicationWaitlistService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ApplicationWaitlistServiceTests {

    private val service = ApplicationWaitlistService()
    private val now = Instant.parse("2026-04-01T00:00:00Z")

    private fun application(status: ApplicationStatus = ApplicationStatus.SUBMITTED) = Application(
        id = "app1",
        buyerUserId = "u1",
        litterId = "l1",
        status = status,
        submittedAt = now
    )

    // ── canTransition / transition ───────────────────────────────────────────

    @Test
    fun submittedCanMoveToUnderReview() {
        assertTrue(service.canTransition(ApplicationStatus.SUBMITTED, ApplicationStatus.UNDER_REVIEW))
    }

    @Test
    fun submittedCannotMoveDirectlyToApproved() {
        assertFalse(service.canTransition(ApplicationStatus.SUBMITTED, ApplicationStatus.APPROVED))
    }

    @Test
    fun declinedIsTerminal() {
        assertFalse(service.canTransition(ApplicationStatus.DECLINED, ApplicationStatus.UNDER_REVIEW))
    }

    @Test
    fun waitlistedCanReturnToUnderReview() {
        assertTrue(service.canTransition(ApplicationStatus.WAITLISTED, ApplicationStatus.UNDER_REVIEW))
    }

    @Test
    fun approvedCanBecomeMatched() {
        assertTrue(service.canTransition(ApplicationStatus.APPROVED, ApplicationStatus.MATCHED))
    }

    @Test
    fun transitionUpdatesStatusAndReviewer() {
        val updated = service.transition(
            application(ApplicationStatus.SUBMITTED), ApplicationStatus.UNDER_REVIEW, "admin1", now
        )
        assertEquals(ApplicationStatus.UNDER_REVIEW, updated.status)
        assertEquals("admin1", updated.reviewedBy)
        assertEquals(now, updated.reviewedAt)
    }

    @Test
    fun transitionThrowsForIllegalMove() {
        assertFailsWith<IllegalArgumentException> {
            service.transition(application(ApplicationStatus.DECLINED), ApplicationStatus.APPROVED, "admin1", now)
        }
    }

    // ── waitlist ordering ─────────────────────────────────────────────────────

    private fun entry(id: String, position: Int, status: WaitlistStatus = WaitlistStatus.ACTIVE) = WaitlistEntry(
        id = id,
        litterId = "l1",
        buyerUserId = id,
        position = position,
        status = status,
        createdAt = now,
        updatedAt = now
    )

    @Test
    fun nextWaitlistPositionIsOneForEmptyList() {
        assertEquals(1, service.nextWaitlistPosition(emptyList()))
    }

    @Test
    fun nextWaitlistPositionIsMaxPlusOne() {
        val entries = listOf(entry("a", 1), entry("b", 2), entry("c", 3))
        assertEquals(4, service.nextWaitlistPosition(entries))
    }

    @Test
    fun activeEntriesInOrderExcludesNonActiveAndSortsByPosition() {
        val entries = listOf(
            entry("c", 3), entry("a", 1), entry("b", 2, WaitlistStatus.REMOVED)
        )
        val ordered = service.activeEntriesInOrder(entries)
        assertEquals(listOf("a", "c"), ordered.map { it.id })
    }

    @Test
    fun removeAndCompactReindexesRemainingActiveEntries() {
        val entries = listOf(entry("a", 1), entry("b", 2), entry("c", 3))
        val result = service.removeAndCompact(entries, "b")
        val active = result.filter { it.status == WaitlistStatus.ACTIVE }.sortedBy { it.position }
        assertEquals(listOf("a" to 1, "c" to 2), active.map { it.id to it.position })
    }

    @Test
    fun removeAndCompactLeavesInactiveEntriesUntouched() {
        val entries = listOf(entry("a", 1), entry("b", 2, WaitlistStatus.CONVERTED), entry("c", 3))
        val result = service.removeAndCompact(entries, "a")
        val converted = result.first { it.id == "b" }
        assertEquals(WaitlistStatus.CONVERTED, converted.status)
    }
}
