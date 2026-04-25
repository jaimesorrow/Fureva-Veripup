package com.fureva.veripup.service

import com.fureva.veripup.model.BreederOnboardingSubmission
import com.fureva.veripup.model.OnboardingAgreementType

class BreederOnboardingService {
    private val requiredAgreements = setOf(
        OnboardingAgreementType.GOOD_BREEDING_INTENTIONS,
        OnboardingAgreementType.HUMANE_CARE_STANDARDS,
        OnboardingAgreementType.ACCURATE_LISTING_DISCLOSURES,
        OnboardingAgreementType.PLATFORM_PAYMENT_AND_ADOPTION_RULES,
        OnboardingAgreementType.APP_TERMS_AND_PRIVACY_POLICY
    )

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
