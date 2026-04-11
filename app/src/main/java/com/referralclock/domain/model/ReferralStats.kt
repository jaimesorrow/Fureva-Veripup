package com.referralclock.domain.model

data class ReferralStats(
    val totalReferrals: Int,
    val totalCreditsEarned: Int,
    val totalBookingsFromReferrals: Int,
    val activeCodesCount: Int
)
