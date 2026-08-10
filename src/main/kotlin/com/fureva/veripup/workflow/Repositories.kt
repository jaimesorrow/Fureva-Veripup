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
    fun findByBreederId(breederId: String): VerificationReviewRecord?
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
    private val records = ConcurrentHashMap<String, VerificationReviewRecord>()

    override fun save(record: VerificationReviewRecord): VerificationReviewRecord {
        records[record.breederId] = record
        return record
    }

    override fun findByBreederId(breederId: String): VerificationReviewRecord? = records[breederId]

    override fun findAll(): List<VerificationReviewRecord> =
        records.values.sortedByDescending { it.submittedAt }

    override fun findPendingReview(): List<VerificationReviewRecord> =
        findAll().filter { it.status == VerificationReviewStatus.READY_FOR_ADMIN_REVIEW }
}
