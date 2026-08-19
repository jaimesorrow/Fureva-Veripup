package com.fureva.veripup

import com.fureva.veripup.integration.MockPaymentProvider
import com.fureva.veripup.model.PaymentStatus
import com.fureva.veripup.model.Reservation
import com.fureva.veripup.model.ReservationStatus
import com.fureva.veripup.service.ReservationService
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ReservationServiceTests {

    private val now = Instant.parse("2026-06-01T00:00:00Z")

    private fun service() = ReservationService(MockPaymentProvider())

    private fun reservation() = Reservation(
        id = "res1",
        litterId = "l1",
        puppyId = "p1",
        buyerUserId = "u1",
        breederProfileId = "bp1",
        amount = BigDecimal("500"),
        refundPolicySummary = "Fully refundable until go-home date confirmed.",
        refundPolicyAcknowledged = true,
        paymentProvider = "stripe",
        providerPaymentId = "pi_mock_1",
        createdAt = now,
        updatedAt = now
    )

    // ── createReservation ─────────────────────────────────────────────────────

    @Test
    fun createReservationFailsWithoutRefundPolicyAcknowledgement() {
        assertFailsWith<IllegalArgumentException> {
            service().createReservation(
                "l1", "p1", "u1", "bp1", BigDecimal("500"), "Refundable", refundPolicyAcknowledged = false, now = now
            )
        }
    }

    @Test
    fun createReservationFailsWithBlankRefundPolicy() {
        assertFailsWith<IllegalArgumentException> {
            service().createReservation(
                "l1", "p1", "u1", "bp1", BigDecimal("500"), "", refundPolicyAcknowledged = true, now = now
            )
        }
    }

    @Test
    fun createReservationSucceedsWithAcknowledgement() {
        val created = service().createReservation(
            "l1", "p1", "u1", "bp1", BigDecimal("500"), "Refundable until confirmed",
            refundPolicyAcknowledged = true, now = now
        )
        assertEquals(PaymentStatus.PENDING, created.paymentStatus)
        assertNotNull(created.providerPaymentId)
    }

    // ── applyWebhookEvent ─────────────────────────────────────────────────────

    @Test
    fun paidEventConfirmsReservation() {
        val updated = service().applyWebhookEvent(reservation(), "evt1", PaymentStatus.PAID, now)
        assertEquals(ReservationStatus.CONFIRMED, updated.reservationStatus)
        assertEquals(PaymentStatus.PAID, updated.paymentStatus)
    }

    @Test
    fun disputedEventFreezesReservation() {
        val updated = service().applyWebhookEvent(reservation(), "evt1", PaymentStatus.DISPUTED, now)
        assertEquals(ReservationStatus.FROZEN, updated.reservationStatus)
    }

    @Test
    fun failedEventCancelsReservation() {
        val updated = service().applyWebhookEvent(reservation(), "evt1", PaymentStatus.FAILED, now)
        assertEquals(ReservationStatus.CANCELLED, updated.reservationStatus)
    }

    @Test
    fun replayingSameEventIdIsIdempotent() {
        val svc = service()
        val first = svc.applyWebhookEvent(reservation(), "evt1", PaymentStatus.PAID, now)
        val replayed = svc.applyWebhookEvent(first, "evt1", PaymentStatus.PENDING, now.plusSeconds(60))
        assertEquals(first, replayed)
    }

    @Test
    fun frozenReservationIgnoresFurtherAutomatedUpdates() {
        val frozen = reservation().copy(reservationStatus = ReservationStatus.FROZEN)
        val result = service().applyWebhookEvent(frozen, "evt2", PaymentStatus.PAID, now)
        assertEquals(ReservationStatus.FROZEN, result.reservationStatus)
    }

    @Test
    fun distinctEventIdsAreBothApplied() {
        val svc = service()
        val paid = svc.applyWebhookEvent(reservation(), "evt1", PaymentStatus.PAID, now)
        val disputed = svc.applyWebhookEvent(paid, "evt2", PaymentStatus.DISPUTED, now.plusSeconds(60))
        assertEquals(ReservationStatus.FROZEN, disputed.reservationStatus)
    }

    // ── freeze / refund ───────────────────────────────────────────────────────

    @Test
    fun freezeSetsFrozenStatus() {
        val frozen = service().freeze(reservation(), now)
        assertEquals(ReservationStatus.FROZEN, frozen.reservationStatus)
    }

    @Test
    fun refundSetsRefundedAndCancelledStatus() {
        val refunded = service().refund(reservation(), now)
        assertEquals(PaymentStatus.REFUNDED, refunded.paymentStatus)
        assertEquals(ReservationStatus.CANCELLED, refunded.reservationStatus)
    }

    @Test
    fun refundFailsWithoutProviderPaymentId() {
        assertFailsWith<IllegalArgumentException> {
            service().refund(reservation().copy(providerPaymentId = null), now)
        }
    }
}
