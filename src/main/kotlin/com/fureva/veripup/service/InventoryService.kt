package com.fureva.veripup.service

import com.fureva.veripup.model.LitterRecord

class InventoryService {
    fun canAcceptDeposit(record: LitterRecord): Boolean {
        if (!record.verifiedByVet || !record.dueDateConfirmed) return false
        val maxDeposits = record.expectedLitterCount - record.completedAdoptions
        return record.reservedDeposits < maxDeposits
    }

    fun canCompleteAdoption(record: LitterRecord): Boolean {
        if (!record.verifiedByVet || !record.dueDateConfirmed) return false
        return record.completedAdoptions < record.expectedLitterCount &&
            record.completedAdoptions < record.reservedDeposits
    }

    fun cappedAvailability(record: LitterRecord): Int {
        val unsold = (record.expectedLitterCount - record.completedAdoptions).coerceAtLeast(0)
        return minOf(record.listedAvailablePuppies, unsold)
    }
}
