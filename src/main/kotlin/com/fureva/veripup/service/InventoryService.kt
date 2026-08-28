package com.fureva.veripup.service

import com.fureva.veripup.model.LitterRecord

/**
 * Enforces inventory rules for a breeder's litter, preventing over-selling of
 * deposits or adoptions relative to the expected litter count.
 *
 * All mutating decisions (accepting a deposit, completing an adoption) require
 * both vet verification and a confirmed due date on the [LitterRecord].
 */
class InventoryService {
    /**
     * Returns `true` if the litter can accept one more deposit reservation.
     *
     * A deposit slot is available only when:
     * - The litter has been verified by a vet.
     * - The due date has been confirmed.
     * - The number of currently reserved deposits is less than the number of
     *   puppies that remain un-adopted (i.e. `expectedLitterCount - completedAdoptions`).
     *
     * @param record Current state of the litter.
     * @return `true` if a new deposit may be accepted; `false` otherwise.
     */
    fun canAcceptDeposit(record: LitterRecord): Boolean {
        if (!record.verifiedByVet || !record.dueDateConfirmed) return false
        val maxDeposits = record.expectedLitterCount - record.completedAdoptions
        return record.reservedDeposits < maxDeposits
    }

    /**
     * Returns `true` if at least one adoption can be completed for this litter.
     *
     * An adoption may be completed only when:
     * - The litter has been verified by a vet.
     * - The due date has been confirmed.
     * - The total completed adoptions is less than both the expected litter
     *   count and the number of reserved deposits (i.e. there is a paid deposit
     *   backing the adoption).
     *
     * @param record Current state of the litter.
     * @return `true` if an adoption can be finalised; `false` otherwise.
     */
    fun canCompleteAdoption(record: LitterRecord): Boolean {
        if (!record.verifiedByVet || !record.dueDateConfirmed) return false
        return record.completedAdoptions < record.expectedLitterCount &&
            record.completedAdoptions < record.reservedDeposits
    }

    /**
     * Returns the accurate public availability count for a litter, capped so
     * it never exceeds the number of puppies that have not yet been adopted.
     *
     * The breeder may list more puppies as available than actually remain; this
     * function clamps that number to `max(0, expectedLitterCount - completedAdoptions)`
     * to prevent inflated or misleading availability figures.
     *
     * @param record Current state of the litter.
     * @return The effective number of puppies available for adoption, between
     *   0 and [LitterRecord.listedAvailablePuppies] inclusive.
     */
    fun cappedAvailability(record: LitterRecord): Int {
        val unsold = (record.expectedLitterCount - record.completedAdoptions).coerceAtLeast(0)
        return minOf(record.listedAvailablePuppies, unsold)
    }
}
