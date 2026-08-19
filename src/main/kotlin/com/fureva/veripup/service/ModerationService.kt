package com.fureva.veripup.service

import com.fureva.veripup.model.AuditLogEntry
import com.fureva.veripup.model.ModerationActionType
import com.fureva.veripup.model.Report
import com.fureva.veripup.model.ReportStatus
import java.time.Instant

/** Every moderation action requires a reason and writes an immutable audit-log entry. */
class ModerationService {

    private val auditLog = mutableListOf<AuditLogEntry>()

    val auditLogEntries: List<AuditLogEntry> get() = auditLog.toList()

    fun assign(report: Report, adminId: String): Report =
        report.copy(assignedAdminId = adminId, status = ReportStatus.IN_REVIEW)

    fun recordAction(
        adminUserId: String,
        actionType: ModerationActionType,
        report: Report,
        reason: String,
        now: Instant
    ): AuditLogEntry {
        require(reason.isNotBlank()) { "A moderation action requires a reason" }
        val entry = AuditLogEntry(
            id = "audit_${now.toEpochMilli()}_${auditLog.size}",
            adminUserId = adminUserId,
            actionType = actionType,
            targetType = report.targetType,
            targetId = report.targetId,
            reason = reason,
            createdAt = now
        )
        auditLog += entry
        return entry
    }

    fun resolve(
        report: Report,
        adminUserId: String,
        resolutionNotes: String,
        now: Instant
    ): Pair<Report, AuditLogEntry> {
        val entry = recordAction(adminUserId, ModerationActionType.RESOLVE_REPORT, report, resolutionNotes, now)
        val resolved = report.copy(
            status = ReportStatus.RESOLVED,
            resolutionNotes = resolutionNotes,
            resolvedAt = now
        )
        return resolved to entry
    }
}
