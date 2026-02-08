package com.fureva.veripup.service

import com.fureva.veripup.integration.AkcVerificationProvider
import com.fureva.veripup.integration.ClinicVerificationProvider
import com.fureva.veripup.model.VerificationSubmission

class VerificationService(
    private val clinicVerificationProvider: ClinicVerificationProvider,
    private val akcVerificationProvider: AkcVerificationProvider
) {
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
