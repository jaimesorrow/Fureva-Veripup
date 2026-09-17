package com.fureva.veripup.integration

/**
 * Performs deepfake / liveness detection on video artifacts uploaded by
 * breeders during the verification pipeline.
 */
interface DeepfakeProvider {
    /**
     * Verifies that the given video artifact represents a real, live person
     * rather than a synthetic or pre-recorded video.
     *
     * @param videoArtifactId Platform-assigned identifier for the uploaded video.
     * @return `true` if the video passes liveness / anti-deepfake checks.
     */
    fun verifyLiveness(videoArtifactId: String): Boolean
}

/**
 * Validates that a veterinary clinic referenced in a verification submission
 * actually exists and is reachable.
 */
interface ClinicVerificationProvider {
    /**
     * Checks whether a registered veterinary clinic is on record for the given
     * phone number.
     *
     * @param phoneNumber The clinic's phone number to look up.
     * @return `true` if a clinic is found for the given number.
     */
    fun clinicExistsByPhone(phoneNumber: String): Boolean
}

/**
 * Validates breeder membership numbers against the American Kennel Club (AKC)
 * registry.
 */
interface AkcVerificationProvider {
    /**
     * Confirms that the supplied AKC member number is active and belongs to a
     * registered breeder.
     *
     * @param memberNumber The AKC member number to verify (e.g. "AKC-12345").
     * @return `true` if the number is valid and active in the AKC registry.
     */
    fun verifyMemberNumber(memberNumber: String): Boolean
}

/**
 * Abstraction over an SMS delivery service used by [com.fureva.veripup.service.AlertsService]
 * to send "Verified Update" notifications to opted-in users.
 */
interface SmsGateway {
    /**
     * Sends an SMS message to the specified phone number.
     *
     * @param to Recipient phone number (E.164 or standard US format).
     * @param message Text body of the message.
     */
    fun send(to: String, message: String)
}

/**
 * Handles monetary transactions on the platform, including deposit holds and
 * adoption payment transfers.
 */
interface PaymentProcessor {
    /**
     * Places a hold on the specified amount and transfers funds to the given
     * recipient once the hold is released.
     *
     * @param amountCents Transaction amount in US cents.
     * @param transferTargetId Platform identifier of the account to receive the funds.
     * @return A unique transaction reference ID for tracking and receipts.
     */
    fun holdAndTransfer(amountCents: Long, transferTargetId: String): String
}

/**
 * In-memory [DeepfakeProvider] for testing. A video artifact is considered
 * live if its ID starts with the prefix `"live_"`.
 */
class MockDeepfakeProvider : DeepfakeProvider {
    override fun verifyLiveness(videoArtifactId: String): Boolean = videoArtifactId.startsWith("live_")
}

/**
 * In-memory [ClinicVerificationProvider] for testing. A phone number is
 * treated as valid if it contains at least 10 characters.
 */
class MockClinicVerificationProvider : ClinicVerificationProvider {
    override fun clinicExistsByPhone(phoneNumber: String): Boolean = phoneNumber.length >= 10
}

/**
 * In-memory [AkcVerificationProvider] for testing. A member number is
 * treated as valid if it starts with the prefix `"AKC-"`.
 */
class MockAkcVerificationProvider : AkcVerificationProvider {
    override fun verifyMemberNumber(memberNumber: String): Boolean = memberNumber.startsWith("AKC-")
}
