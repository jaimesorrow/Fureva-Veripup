package com.referralclock.domain.repository

import com.referralclock.core.util.Result
import com.referralclock.domain.model.Referral
import com.referralclock.domain.model.ReferralCode
import com.referralclock.domain.model.ReferralStats

interface ReferralRepository {
    suspend fun getStats(userId: String): Result<ReferralStats>
    /** Returns the most recent [limit] codes owned by the user, newest first. */
    suspend fun getRecentCodes(userId: String, limit: Int = 5): Result<List<ReferralCode>>
    suspend fun getRecentReferrals(userId: String, limit: Int = 5): Result<List<Referral>>
    /** Generates a new shareable code for the user, enforcing server-side rate limits. */
    suspend fun generateCode(userId: String): Result<ReferralCode>
    suspend fun redeemCode(code: String, newClientId: String): Result<Referral>
}
