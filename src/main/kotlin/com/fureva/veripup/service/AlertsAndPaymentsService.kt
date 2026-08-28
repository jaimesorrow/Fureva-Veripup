package com.fureva.veripup.service

import com.fureva.veripup.integration.SmsGateway
import com.fureva.veripup.model.SmsPreference
import com.fureva.veripup.model.VerifiedEventType

/**
 * Delivers "Verified Update" SMS notifications to opted-in users when a
 * verified breeder triggers a relevant platform event.
 *
 * Alerts are gated by two independent conditions:
 * 1. The originating breeder must hold verified status (checked via [canTriggerEvent]).
 * 2. The recipient user must have opted in to SMS alerts (checked via
 *    [SmsPreference.optedInVerifiedAlerts] inside [sendVerifiedUpdate]).
 *
 * @param smsGateway The underlying SMS delivery provider.
 */
class AlertsService(private val smsGateway: SmsGateway) {
    /**
     * Sends an SMS alert to the user identified by [preference], prefixed with
     * the "Verified Update:" label.
     *
     * If the user has not opted in ([SmsPreference.optedInVerifiedAlerts] is
     * `false`), the function returns immediately without sending anything.
     *
     * @param preference The recipient's SMS notification preferences.
     * @param text The body of the alert message (without the "Verified Update:" prefix).
     */
    fun sendVerifiedUpdate(preference: SmsPreference, text: String) {
        if (!preference.optedInVerifiedAlerts) return
        smsGateway.send(preference.phoneNumber, "Verified Update: $text")
    }

    /**
     * Returns `true` if the given event type may be used to trigger a
     * "Verified Update" alert for the supplied breeder.
     *
     * Currently all [VerifiedEventType] values are permitted, but the breeder
     * must have verified status — unverified breeders cannot send alerts
     * regardless of the event type.
     *
     * @param eventType The category of platform event being evaluated.
     * @param breederVerified Whether the breeder initiating the event is verified.
     * @return `true` if the event may trigger an alert; `false` otherwise.
     */
    fun canTriggerEvent(eventType: VerifiedEventType, breederVerified: Boolean): Boolean {
        if (!breederVerified) return false
        return when (eventType) {
            VerifiedEventType.NEW_PUPS_POSTED,
            VerifiedEventType.AVAILABILITY_CHANGED,
            VerifiedEventType.UPCOMING_LITTER_POLICY_ALLOWED,
            VerifiedEventType.VET_CONFIRMED_DEPOSIT_UNLOCK -> true
        }
    }
}

/**
 * Defines the platform's fee schedule for breeder subscriptions and
 * transaction-based charges.
 *
 * All fee calculations are pure functions — they accept an amount in US cents
 * and return the corresponding fee in US cents.
 */
object FeePolicy {
    /** Monthly subscription fee charged to active breeders (USD). */
    const val subscriptionMonthlyUsd = 29.99

    /** Platform fee rate applied to deposit transactions (7%). */
    const val depositFeeRate = 0.07

    /** Platform fee rate applied to completed adoption transactions (8%). */
    const val adoptionFeeRate = 0.08

    /**
     * Calculates the platform fee for a deposit transaction.
     *
     * @param amountCents The gross deposit amount in US cents.
     * @return The platform fee in US cents (truncated, not rounded).
     */
    fun depositFee(amountCents: Long): Long = (amountCents * depositFeeRate).toLong()

    /**
     * Calculates the platform fee for a completed adoption transaction.
     *
     * @param amountCents The gross adoption payment amount in US cents.
     * @return The platform fee in US cents (truncated, not rounded).
     */
    fun adoptionFee(amountCents: Long): Long = (amountCents * adoptionFeeRate).toLong()
}
