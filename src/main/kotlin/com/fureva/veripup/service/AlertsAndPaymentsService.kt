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
    const val depositFeeBasisPoints = 700L
    const val adoptionFeeBasisPoints = 800L
    private const val basisPointsDenominator = 10_000L

    fun depositFee(amountCents: Long): Long = feeFromBasisPoints(amountCents, depositFeeBasisPoints)
    fun adoptionFee(amountCents: Long): Long = feeFromBasisPoints(amountCents, adoptionFeeBasisPoints)

    private fun feeFromBasisPoints(amountCents: Long, basisPoints: Long): Long {
        val raw = amountCents * basisPoints
        val halfUpOffset = basisPointsDenominator / 2
        return (raw + halfUpOffset) / basisPointsDenominator
    }
}
