package com.fureva.veripup.integration

interface DeepfakeProvider {
    fun verifyLiveness(videoArtifactId: String): Boolean
}

interface ClinicVerificationProvider {
    fun clinicExistsByPhone(phoneNumber: String): Boolean
}

interface AkcVerificationProvider {
    fun verifyMemberNumber(memberNumber: String): Boolean
}

interface SmsGateway {
    fun send(to: String, message: String)
}

interface PaymentProcessor {
    fun holdAndTransfer(amountCents: Long, transferTargetId: String): String
}

class MockDeepfakeProvider : DeepfakeProvider {
    override fun verifyLiveness(videoArtifactId: String): Boolean = videoArtifactId.startsWith("live_")
}

class MockClinicVerificationProvider : ClinicVerificationProvider {
    override fun clinicExistsByPhone(phoneNumber: String): Boolean = phoneNumber.length >= 10
}

class MockAkcVerificationProvider : AkcVerificationProvider {
    override fun verifyMemberNumber(memberNumber: String): Boolean = memberNumber.startsWith("AKC-")
}
