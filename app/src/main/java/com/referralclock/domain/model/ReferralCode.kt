package com.referralclock.domain.model

import java.time.Instant

data class ReferralCode(
    val code: String,
    val ownerId: String,
    val createdAt: Instant,
    val expiresAt: Instant? = null,
    val timesRedeemed: Int = 0,
    val maxRedemptions: Int? = null,
    val isActive: Boolean = true
)
