package com.fureva.veripup.integration

/** S3/R2-compatible object storage for identity documents, litter media, and health records. */
interface MediaStorageProvider {
    fun uploadObject(key: String, contentType: String): String
    fun cdnUrl(storageKey: String): String
}

/** Malware scanning for any uploaded file before it is served publicly or reviewed by admins. */
interface MalwareScanProvider {
    fun scan(storageKey: String): Boolean
}

/** Stripe-style payment intents for reservation deposits. */
interface PaymentProvider {
    fun createPaymentIntent(amountCents: Long, currency: String, metadata: Map<String, String>): String
    fun refund(providerPaymentId: String, amountCents: Long? = null): String
}

/** Twilio-style SMS for phone verification and (opt-in) notifications. */
interface SmsProvider {
    fun sendVerificationCode(phone: String): String
    fun sendNotification(phone: String, message: String)
}

/** Transactional email for verification, applications, reservations, and reports. */
interface EmailProvider {
    fun sendTransactional(to: String, template: String, data: Map<String, String>)
}

class InMemoryMediaStorageProvider(private val cdnBase: String = "https://cdn.furevaveripup.example") :
    MediaStorageProvider {
    private val stored = mutableMapOf<String, String>()

    override fun uploadObject(key: String, contentType: String): String {
        stored[key] = contentType
        return "$cdnBase/$key"
    }

    override fun cdnUrl(storageKey: String): String = "$cdnBase/$storageKey"
}

class AlwaysCleanMalwareScanProvider : MalwareScanProvider {
    override fun scan(storageKey: String): Boolean = true
}

class MockPaymentProvider : PaymentProvider {
    private var counter = 0

    override fun createPaymentIntent(amountCents: Long, currency: String, metadata: Map<String, String>): String {
        counter += 1
        return "pi_mock_$counter"
    }

    override fun refund(providerPaymentId: String, amountCents: Long?): String = "re_mock_$providerPaymentId"
}

class MockSmsProvider : SmsProvider {
    val sentNotifications = mutableListOf<Pair<String, String>>()

    override fun sendVerificationCode(phone: String): String = "123456"

    override fun sendNotification(phone: String, message: String) {
        sentNotifications += phone to message
    }
}

class MockEmailProvider : EmailProvider {
    val sent = mutableListOf<Triple<String, String, Map<String, String>>>()

    override fun sendTransactional(to: String, template: String, data: Map<String, String>) {
        sent += Triple(to, template, data)
    }
}
