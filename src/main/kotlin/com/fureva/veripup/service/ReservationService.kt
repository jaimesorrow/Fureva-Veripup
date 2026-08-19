package com.fureva.veripup.service

import com.fureva.veripup.integration.PaymentProvider
import com.fureva.veripup.model.PaymentStatus
import com.fureva.veripup.model.Reservation
import com.fureva.veripup.model.ReservationStatus
import java.time.Instant

/**
 * Reservation deposits never store raw card data; only a provider payment intent id is kept.
 * Payment webhooks must be idempotent, and a disputed reservation can be frozen from further
 * automated payout actions by an admin.
 */
class ReservationService(private val paymentProvider: PaymentProvider) {

    fun createReservation(
        litterId: String,
        puppyId: String,
        buyerUserId: String,
        breederProfileId: String,
        amount: java.math.BigDecimal,
        refundPolicySummary: String,
        refundPolicyAcknowledged: Boolean,
        now: Instant
    ): Reservation {
        require(refundPolicyAcknowledged) { "Deposit payment requires an explicit refund-policy acknowledgement" }
        require(refundPolicySummary.isNotBlank()) { "A refund policy must be defined before accepting a deposit" }

        val intentId = paymentProvider.createPaymentIntent(
            amountCents = amount.movePointRight(2).toLong(),
            currency = "USD",
            metadata = mapOf("litterId" to litterId, "puppyId" to puppyId, "buyerUserId" to buyerUserId)
        )

        return Reservation(
            id = "res_${now.toEpochMilli()}",
            litterId = litterId,
            puppyId = puppyId,
            buyerUserId = buyerUserId,
            breederProfileId = breederProfileId,
            amount = amount,
            refundPolicySummary = refundPolicySummary,
            refundPolicyAcknowledged = true,
            paymentProvider = "stripe",
            providerPaymentId = intentId,
            createdAt = now,
            updatedAt = now
        )
    }

    /**
     * Applies a payment webhook event. Idempotent: replaying the same event id is a no-op.
     * A frozen reservation ignores further automated updates until an admin unfreezes it.
     */
    fun applyWebhookEvent(
        reservation: Reservation,
        eventId: String,
        newPaymentStatus: PaymentStatus,
        now: Instant
    ): Reservation {
        if (reservation.reservationStatus == ReservationStatus.FROZEN) return reservation
        if (reservation.lastProcessedEventId == eventId) return reservation

        val newReservationStatus = when (newPaymentStatus) {
            PaymentStatus.PAID -> ReservationStatus.CONFIRMED
            PaymentStatus.FAILED -> ReservationStatus.CANCELLED
            PaymentStatus.REFUNDED -> ReservationStatus.CANCELLED
            PaymentStatus.DISPUTED -> ReservationStatus.FROZEN
            PaymentStatus.PENDING -> reservation.reservationStatus
        }

        return reservation.copy(
            paymentStatus = newPaymentStatus,
            reservationStatus = newReservationStatus,
            lastProcessedEventId = eventId,
            updatedAt = now
        )
    }

    fun freeze(reservation: Reservation, now: Instant): Reservation =
        reservation.copy(reservationStatus = ReservationStatus.FROZEN, updatedAt = now)

    fun refund(reservation: Reservation, now: Instant): Reservation {
        val providerPaymentId = requireNotNull(reservation.providerPaymentId) { "Reservation has no payment to refund" }
        paymentProvider.refund(providerPaymentId)
        return reservation.copy(
            paymentStatus = PaymentStatus.REFUNDED,
            reservationStatus = ReservationStatus.CANCELLED,
            updatedAt = now
        )
    }
}
