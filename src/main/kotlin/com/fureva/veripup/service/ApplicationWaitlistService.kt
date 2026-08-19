package com.fureva.veripup.service

import com.fureva.veripup.model.Application
import com.fureva.veripup.model.ApplicationStatus
import com.fureva.veripup.model.WaitlistEntry
import com.fureva.veripup.model.WaitlistStatus
import java.time.Instant

class ApplicationWaitlistService {

    companion object {
        private val ALLOWED_TRANSITIONS: Map<ApplicationStatus, Set<ApplicationStatus>> = mapOf(
            ApplicationStatus.SUBMITTED to setOf(
                ApplicationStatus.UNDER_REVIEW, ApplicationStatus.WITHDRAWN
            ),
            ApplicationStatus.UNDER_REVIEW to setOf(
                ApplicationStatus.APPROVED,
                ApplicationStatus.WAITLISTED,
                ApplicationStatus.DECLINED,
                ApplicationStatus.WITHDRAWN
            ),
            ApplicationStatus.APPROVED to setOf(ApplicationStatus.MATCHED, ApplicationStatus.WITHDRAWN),
            ApplicationStatus.WAITLISTED to setOf(
                ApplicationStatus.UNDER_REVIEW, ApplicationStatus.DECLINED, ApplicationStatus.WITHDRAWN
            ),
            ApplicationStatus.DECLINED to emptySet(),
            ApplicationStatus.WITHDRAWN to emptySet(),
            ApplicationStatus.MATCHED to emptySet()
        )
    }

    fun canTransition(from: ApplicationStatus, to: ApplicationStatus): Boolean =
        ALLOWED_TRANSITIONS[from]?.contains(to) == true

    fun transition(application: Application, to: ApplicationStatus, reviewerAdminId: String?, now: Instant): Application {
        require(canTransition(application.status, to)) {
            "Cannot move application from ${application.status} to $to"
        }
        return application.copy(status = to, reviewedAt = now, reviewedBy = reviewerAdminId ?: application.reviewedBy)
    }

    fun nextWaitlistPosition(existingEntries: List<WaitlistEntry>): Int =
        (existingEntries.maxOfOrNull { it.position } ?: 0) + 1

    fun activeEntriesInOrder(entries: List<WaitlistEntry>): List<WaitlistEntry> =
        entries.filter { it.status == WaitlistStatus.ACTIVE }.sortedBy { it.position }

    fun removeAndCompact(entries: List<WaitlistEntry>, entryId: String): List<WaitlistEntry> {
        val remaining = entries.filterNot { it.id == entryId }
        val active = remaining.filter { it.status == WaitlistStatus.ACTIVE }.sortedBy { it.position }
        val inactive = remaining.filter { it.status != WaitlistStatus.ACTIVE }
        val reindexed = active.mapIndexed { index, entry -> entry.copy(position = index + 1) }
        return reindexed + inactive
    }
}
