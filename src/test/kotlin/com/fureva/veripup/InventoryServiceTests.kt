package com.fureva.veripup

import com.fureva.veripup.model.LitterRecord
import com.fureva.veripup.service.InventoryService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InventoryServiceTests {

    private val inventory = InventoryService()

    private fun verifiedRecord(
        expectedLitterCount: Int = 4,
        reservedDeposits: Int = 0,
        completedAdoptions: Int = 0,
        listedAvailablePuppies: Int = 4
    ) = LitterRecord(
        breederId = "b1",
        expectedLitterCount = expectedLitterCount,
        dueDateConfirmed = true,
        verifiedByVet = true,
        listedAvailablePuppies = listedAvailablePuppies,
        reservedDeposits = reservedDeposits,
        completedAdoptions = completedAdoptions
    )

    // ── canAcceptDeposit ───────────────────────────────────────────────────────

    @Test
    fun canAcceptDepositWhenSlotsAvailable() {
        val record = verifiedRecord(expectedLitterCount = 4, reservedDeposits = 2, completedAdoptions = 0)
        assertTrue(inventory.canAcceptDeposit(record))
    }

    @Test
    fun cannotAcceptDepositWhenAllSlotsReserved() {
        val record = verifiedRecord(expectedLitterCount = 3, reservedDeposits = 3, completedAdoptions = 0)
        assertFalse(inventory.canAcceptDeposit(record))
    }

    @Test
    fun cannotAcceptDepositWhenNotVerifiedByVet() {
        val record = verifiedRecord().copy(verifiedByVet = false)
        assertFalse(inventory.canAcceptDeposit(record))
    }

    @Test
    fun cannotAcceptDepositWhenDueDateNotConfirmed() {
        val record = verifiedRecord().copy(dueDateConfirmed = false)
        assertFalse(inventory.canAcceptDeposit(record))
    }

    @Test
    fun depositsCountsAgainstRemainingAfterAdoptions() {
        // 3 expected, 1 adopted → 2 slots; 2 reserved → no more deposits
        val record = verifiedRecord(expectedLitterCount = 3, reservedDeposits = 2, completedAdoptions = 1)
        assertFalse(inventory.canAcceptDeposit(record))
    }

    @Test
    fun canAcceptDepositWhenOneSlotRemainsAfterAdoptions() {
        // 3 expected, 1 adopted → 2 slots; 1 reserved → 1 more deposit allowed
        val record = verifiedRecord(expectedLitterCount = 3, reservedDeposits = 1, completedAdoptions = 1)
        assertTrue(inventory.canAcceptDeposit(record))
    }

    // ── canCompleteAdoption ────────────────────────────────────────────────────

    @Test
    fun canCompleteAdoptionWhenDepositsExceedAdoptions() {
        val record = verifiedRecord(expectedLitterCount = 4, reservedDeposits = 2, completedAdoptions = 1)
        assertTrue(inventory.canCompleteAdoption(record))
    }

    @Test
    fun cannotCompleteAdoptionWhenAdoptionsEqualExpectedLitter() {
        val record = verifiedRecord(expectedLitterCount = 3, reservedDeposits = 3, completedAdoptions = 3)
        assertFalse(inventory.canCompleteAdoption(record))
    }

    @Test
    fun cannotCompleteAdoptionWhenAdoptionsEqualDeposits() {
        val record = verifiedRecord(expectedLitterCount = 4, reservedDeposits = 2, completedAdoptions = 2)
        assertFalse(inventory.canCompleteAdoption(record))
    }

    @Test
    fun cannotCompleteAdoptionWhenNotVerifiedByVet() {
        val record = verifiedRecord(reservedDeposits = 2, completedAdoptions = 0).copy(verifiedByVet = false)
        assertFalse(inventory.canCompleteAdoption(record))
    }

    @Test
    fun cannotCompleteAdoptionWhenDueDateNotConfirmed() {
        val record = verifiedRecord(reservedDeposits = 2, completedAdoptions = 0).copy(dueDateConfirmed = false)
        assertFalse(inventory.canCompleteAdoption(record))
    }

    @Test
    fun cannotCompleteAdoptionWhenNoDepositsExist() {
        val record = verifiedRecord(expectedLitterCount = 4, reservedDeposits = 0, completedAdoptions = 0)
        assertFalse(inventory.canCompleteAdoption(record))
    }

    // ── cappedAvailability ─────────────────────────────────────────────────────

    @Test
    fun cappedAvailabilityIsListedWhenListedBelowUnsold() {
        val record = verifiedRecord(expectedLitterCount = 4, completedAdoptions = 0, listedAvailablePuppies = 2)
        assertEquals(2, inventory.cappedAvailability(record))
    }

    @Test
    fun cappedAvailabilityIsUnsoldWhenListedExceedsUnsold() {
        // 3 expected, 2 adopted → 1 unsold; listed = 5
        val record = verifiedRecord(expectedLitterCount = 3, completedAdoptions = 2, listedAvailablePuppies = 5)
        assertEquals(1, inventory.cappedAvailability(record))
    }

    @Test
    fun cappedAvailabilityIsZeroWhenAllAdopted() {
        val record = verifiedRecord(expectedLitterCount = 3, completedAdoptions = 3, listedAvailablePuppies = 3)
        assertEquals(0, inventory.cappedAvailability(record))
    }

    @Test
    fun cappedAvailabilityIsZeroWhenListedIsZero() {
        val record = verifiedRecord(expectedLitterCount = 4, completedAdoptions = 0, listedAvailablePuppies = 0)
        assertEquals(0, inventory.cappedAvailability(record))
    }

    @Test
    fun cappedAvailabilityNeverGoesNegative() {
        // More completedAdoptions than expectedLitterCount (data integrity edge case)
        val record = verifiedRecord(expectedLitterCount = 2, completedAdoptions = 5, listedAvailablePuppies = 3)
        assertEquals(0, inventory.cappedAvailability(record))
    }
}
