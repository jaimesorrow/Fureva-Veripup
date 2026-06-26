package com.fureva.veripup

import com.fureva.veripup.service.FeePolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeePolicyTests {

    // ── Constants ─────────────────────────────────────────────────────────────

    @Test
    fun subscriptionMonthlyUsdIsCorrect() {
        assertEquals(29.99, FeePolicy.subscriptionMonthlyUsd, 0.0001)
    }

    @Test
    fun depositFeeRateIsCorrect() {
        assertEquals(0.07, FeePolicy.depositFeeRate, 0.0001)
    }

    @Test
    fun adoptionFeeRateIsCorrect() {
        assertEquals(0.08, FeePolicy.adoptionFeeRate, 0.0001)
    }

    // ── depositFee ─────────────────────────────────────────────────────────────

    @Test
    fun depositFeeOn100Dollars() {
        // $100.00 = 10_000 cents → 7% = $7.00 = 700 cents
        assertEquals(700L, FeePolicy.depositFee(10_000L))
    }

    @Test
    fun depositFeeOn50Dollars() {
        // $50.00 = 5_000 cents → 7% = $3.50 = 350 cents
        assertEquals(350L, FeePolicy.depositFee(5_000L))
    }

    @Test
    fun depositFeeIsZeroOnZeroAmount() {
        assertEquals(0L, FeePolicy.depositFee(0L))
    }

    @Test
    fun depositFeeOnLargeAmount() {
        // $1000.00 = 100_000 cents → 7% = $70.00 = 7000 cents
        assertEquals(7_000L, FeePolicy.depositFee(100_000L))
    }

    @Test
    fun depositFeeIsNonNegative() {
        assertTrue(FeePolicy.depositFee(1L) >= 0L)
    }

    // ── adoptionFee ────────────────────────────────────────────────────────────

    @Test
    fun adoptionFeeOn100Dollars() {
        // $100.00 = 10_000 cents → 8% = $8.00 = 800 cents
        assertEquals(800L, FeePolicy.adoptionFee(10_000L))
    }

    @Test
    fun adoptionFeeOn50Dollars() {
        // $50.00 = 5_000 cents → 8% = $4.00 = 400 cents
        assertEquals(400L, FeePolicy.adoptionFee(5_000L))
    }

    @Test
    fun adoptionFeeIsZeroOnZeroAmount() {
        assertEquals(0L, FeePolicy.adoptionFee(0L))
    }

    @Test
    fun adoptionFeeOnLargeAmount() {
        // $1000.00 = 100_000 cents → 8% = $80.00 = 8000 cents
        assertEquals(8_000L, FeePolicy.adoptionFee(100_000L))
    }

    @Test
    fun adoptionFeeIsNonNegative() {
        assertTrue(FeePolicy.adoptionFee(1L) >= 0L)
    }

    // ── relative rates ─────────────────────────────────────────────────────────

    @Test
    fun adoptionFeeIsHigherThanDepositFeeForSameAmount() {
        val amount = 10_000L
        assertTrue(FeePolicy.adoptionFee(amount) > FeePolicy.depositFee(amount))
    }
}
