package com.fureva.veripup.service

import com.fureva.veripup.integration.AkcVerificationProvider
import com.fureva.veripup.integration.ClinicVerificationProvider
import com.fureva.veripup.model.VerificationSubmission

/**
 * Administers the full breeder verification pipeline and determines whether a
 * [VerificationSubmission] qualifies a breeder for the
 * [com.fureva.veripup.model.TrustBadge.VERIPUP_VERIFIED] badge.
 *
 * Verification is the second stage of the onboarding process (after
 * [BreederOnboardingService]) and is reviewed by a platform admin. It
 * cross-checks identity documents, deepfake-resistant liveness videos,
 * veterinary documentation, and optionally an AKC membership number.
 *
 * @param clinicVerificationProvider Used to confirm that the breeder's
 *   veterinary clinic exists via its phone number.
 * @param akcVerificationProvider Used to validate an optional AKC member
 *   number against the AKC registry.
 */
class VerificationService(
    private val clinicVerificationProvider: ClinicVerificationProvider,
    private val akcVerificationProvider: AkcVerificationProvider
) {
    /**
     * Evaluates a [VerificationSubmission] and returns whether the breeder
     * should be granted verified status.
     *
     * All of the following conditions must be true for approval:
     * 1. Government ID is readable.
     * 2. ID name matches the account sign-up details.
     * 3. First liveness video passed deepfake detection.
     * 4. Veterinary documents are uploaded.
     * 5. Vet documents match the declared breed.
     * 6. Second liveness video passed deepfake detection.
     * 7. Release of Information (ROI) agreement is signed.
     * 8. The supplied clinic phone resolves to a real veterinary clinic.
     * 9. If an AKC number is provided, it must be valid in the AKC registry.
     *    (If no AKC number is provided this check is skipped.)
     *
     * @param submission The completed verification data for a single breeder.
     * @return `true` if all required checks pass and the optional AKC check
     *   (when applicable) also passes; `false` otherwise.
     */
    fun approve(submission: VerificationSubmission): Boolean {
        val requiredChecks = listOf(
            submission.governmentIdReadable,
            submission.idMatchesSignup,
            submission.firstLiveVideoPassedDeepfake,
            submission.vetDocsUploaded,
            submission.vetDocsMatchBreed,
            submission.secondLiveVideoPassedDeepfake,
            submission.roiSigned,
            clinicVerificationProvider.clinicExistsByPhone(submission.clinicPhone)
        )

        if (requiredChecks.any { !it }) return false

        return submission.optionalAkcNumber?.let { akcVerificationProvider.verifyMemberNumber(it) } ?: true
    }
}
