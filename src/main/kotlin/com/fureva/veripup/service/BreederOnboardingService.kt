package com.fureva.veripup.service

import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.OnboardingAgreementType

/**
 * Validates breeder onboarding submissions before they are forwarded to the
 * full verification pipeline.
 *
 * Onboarding is the first step a new breeder must complete. It collects
 * identity documents, veterinary records, and legally-binding agreements. Only
 * once all requirements are satisfied can the breeder proceed to the
 * admin-reviewed [VerificationService] step.
 */
class BreederOnboardingService {
    /**
     * The complete set of agreements every breeder must accept.
     * All five [OnboardingAgreementType] values are required.
     */
    private val requiredAgreements = setOf(
        OnboardingAgreementType.GOOD_BREEDING_INTENTIONS,
        OnboardingAgreementType.HUMANE_CARE_STANDARDS,
        OnboardingAgreementType.ACCURATE_LISTING_DISCLOSURES,
        OnboardingAgreementType.PLATFORM_PAYMENT_AND_ADOPTION_RULES,
        OnboardingAgreementType.APP_TERMS_AND_PRIVACY_POLICY
    )

    /**
     * Returns `true` when the submission satisfies every onboarding requirement
     * and the breeder is ready to enter the verification queue.
     *
     * Requirements checked:
     * - Government ID uploaded
     * - Photo holding government ID uploaded
     * - Veterinary records uploaded
     * - Vet records confirm breeding dogs
     * - Onboarding declaration signed ([BreederOnboardingSubmission.signedAt] is not null)
     * - All five [OnboardingAgreementType] entries accepted
     *
     * @param submission The breeder's onboarding data.
     * @return `true` if all requirements pass; `false` if any are missing.
     */
    fun isReadyForVerification(submission: BreederOnboardingSubmission): Boolean {
        val requiredChecks = listOf(
            submission.governmentIdUploaded,
            submission.photoHoldingGovernmentIdUploaded,
            submission.vetRecordsUploaded,
            submission.vetRecordsCoverBreedingDogs,
            submission.signedAt != null,
            submission.acceptedAgreements.containsAll(requiredAgreements)
        )

        return requiredChecks.all { it }
    }

    /**
     * Returns a human-readable list of unmet requirements for the given
     * submission. Useful for surfacing actionable feedback to the breeder in
     * the UI or admin tooling.
     *
     * An empty list means the submission is complete (equivalent to
     * [isReadyForVerification] returning `true`).
     *
     * @param submission The breeder's onboarding data.
     * @return Ordered list of missing requirement descriptions; empty if none.
     */
    fun missingRequirements(submission: BreederOnboardingSubmission): List<String> {
        val missing = mutableListOf<String>()

        if (!submission.governmentIdUploaded) missing += "Government ID upload"
        if (!submission.photoHoldingGovernmentIdUploaded) missing += "Photo holding government ID"
        if (!submission.vetRecordsUploaded) missing += "Vet records upload"
        if (!submission.vetRecordsCoverBreedingDogs) missing += "Vet records confirming breeding dogs"
        if (submission.signedAt == null) missing += "Onboarding signature"

        val missingAgreements = requiredAgreements - submission.acceptedAgreements
        missingAgreements.forEach { agreement ->
            missing += "Agreement: $agreement"
        }

        return missing
    }
}
