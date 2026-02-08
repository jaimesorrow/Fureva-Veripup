package com.fureva.veripup.service

import com.fureva.veripup.integration.SmsGateway
import com.fureva.veripup.model.SmsPreference
import com.fureva.veripup.model.VerifiedEventType

class AlertsService(private val smsGateway: SmsGateway) {
    fun sendVerifiedUpdate(preference: SmsPreference, text: String) {
        if (!preference.optedInVerifiedAlerts) return
        smsGateway.send(preference.phoneNumber, "Verified Update: $text")
    }

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

object FeePolicy {
    const val subscriptionMonthlyUsd = 29.99
    const val depositFeeRate = 0.07
    const val adoptionFeeRate = 0.08

    fun depositFee(amountCents: Long): Long = (amountCents * depositFeeRate).toLong()
    fun adoptionFee(amountCents: Long): Long = (amountCents * adoptionFeeRate).toLong()
}
