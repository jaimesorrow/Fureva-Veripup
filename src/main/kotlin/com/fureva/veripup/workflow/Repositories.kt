package com.fureva.veripup.workflow

import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.BreederProfile
import java.util.concurrent.ConcurrentHashMap

interface BreederProfileRepository {
    fun save(profile: BreederProfile): BreederProfile
    fun findById(id: String): BreederProfile?
    fun findAll(): List<BreederProfile>
}

interface BreederOnboardingRepository {
    fun save(submission: BreederOnboardingSubmission): BreederOnboardingSubmission
    fun findByBreederId(breederId: String): BreederOnboardingSubmission?
}

interface VerificationReviewRepository {
    fun save(record: VerificationReviewRecord): VerificationReviewRecord
    fun findLatestByBreederId(breederId: String): VerificationReviewRecord?
    fun findAll(): List<VerificationReviewRecord>
    fun findPendingReview(): List<VerificationReviewRecord>
}

class InMemoryBreederProfileRepository : BreederProfileRepository {
    private val breeders = ConcurrentHashMap<String, BreederProfile>()

    override fun save(profile: BreederProfile): BreederProfile {
        breeders[profile.id] = profile
        return profile
    }

    override fun findById(id: String): BreederProfile? = breeders[id]

    override fun findAll(): List<BreederProfile> = breeders.values.sortedBy { it.id }
}

class InMemoryBreederOnboardingRepository : BreederOnboardingRepository {
    private val submissions = ConcurrentHashMap<String, BreederOnboardingSubmission>()

    override fun save(submission: BreederOnboardingSubmission): BreederOnboardingSubmission {
        submissions[submission.breederId] = submission
        return submission
    }

    override fun findByBreederId(breederId: String): BreederOnboardingSubmission? = submissions[breederId]
}

class InMemoryVerificationReviewRepository : VerificationReviewRepository {
    private val records = ConcurrentHashMap<String, MutableList<VerificationReviewRecord>>()

    override fun save(record: VerificationReviewRecord): VerificationReviewRecord {
        records.compute(record.breederId) { _, existing ->
            val storedRecords = existing ?: mutableListOf()
            val currentIndex = storedRecords.indexOfFirst { it.submittedAt == record.submittedAt }
            if (currentIndex >= 0) {
                storedRecords[currentIndex] = record
            } else {
                storedRecords += record
            }
            storedRecords
        }
        return record
    }

    override fun findLatestByBreederId(breederId: String): VerificationReviewRecord? =
        records[breederId]?.maxByOrNull { it.submittedAt }

    override fun findAll(): List<VerificationReviewRecord> =
        records.values
            .flatten()
            .sortedByDescending { it.submittedAt }

    override fun findPendingReview(): List<VerificationReviewRecord> =
        findAll().filter { it.status == VerificationReviewStatus.READY_FOR_ADMIN_REVIEW }
}
