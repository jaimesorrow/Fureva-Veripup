package com.fureva.veripup

import com.fureva.veripup.model.ModerationActionType
import com.fureva.veripup.model.Report
import com.fureva.veripup.model.ReportReason
import com.fureva.veripup.model.ReportStatus
import com.fureva.veripup.model.ReportTargetType
import com.fureva.veripup.service.ModerationService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModerationServiceTests {

    private val now = Instant.parse("2026-07-01T00:00:00Z")

    private fun report() = Report(
        id = "rep1",
        reporterUserId = "u1",
        targetType = ReportTargetType.LISTING,
        targetId = "l1",
        reason = ReportReason.SCAM_CONCERN,
        createdAt = now
    )

    @Test
    fun assignSetsAdminAndInReviewStatus() {
        val service = ModerationService()
        val assigned = service.assign(report(), "admin1")
        assertEquals("admin1", assigned.assignedAdminId)
        assertEquals(ReportStatus.IN_REVIEW, assigned.status)
    }

    @Test
    fun recordActionRequiresNonBlankReason() {
        val service = ModerationService()
        assertFailsWith<IllegalArgumentException> {
            service.recordAction("admin1", ModerationActionType.HIDE_LISTING, report(), "", now)
        }
    }

    @Test
    fun recordActionAppendsToAuditLog() {
        val service = ModerationService()
        service.recordAction("admin1", ModerationActionType.WARN, report(), "First warning", now)
        assertEquals(1, service.auditLogEntries.size)
    }

    @Test
    fun auditLogIsImmutableFromOutside() {
        val service = ModerationService()
        service.recordAction("admin1", ModerationActionType.WARN, report(), "Warned", now)
        val snapshot = service.auditLogEntries
        service.recordAction("admin1", ModerationActionType.HIDE_LISTING, report(), "Hidden", now)
        assertEquals(1, snapshot.size)
        assertEquals(2, service.auditLogEntries.size)
    }

    @Test
    fun resolveMarksReportResolvedAndWritesAuditEntry() {
        val service = ModerationService()
        val (resolved, entry) = service.resolve(report(), "admin1", "No violation found", now)
        assertEquals(ReportStatus.RESOLVED, resolved.status)
        assertEquals(now, resolved.resolvedAt)
        assertEquals(ModerationActionType.RESOLVE_REPORT, entry.actionType)
        assertTrue(service.auditLogEntries.contains(entry))
    }

    @Test
    fun multipleActionsAccumulateInOrder() {
        val service = ModerationService()
        service.recordAction("admin1", ModerationActionType.WARN, report(), "r1", now)
        service.recordAction("admin1", ModerationActionType.FREEZE_DEPOSIT, report(), "r2", now.plusSeconds(1))
        assertEquals(
            listOf(ModerationActionType.WARN, ModerationActionType.FREEZE_DEPOSIT),
            service.auditLogEntries.map { it.actionType }
        )
    }
}
