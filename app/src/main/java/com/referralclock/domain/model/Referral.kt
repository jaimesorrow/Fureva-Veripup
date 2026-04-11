package com.referralclock.domain.model

import java.time.Instant

data class Referral(
    val id: String,
    val referrerId: String,
    val referrerName: String,
    val newClientId: String,
    val newClientName: String,
    val code: String,
    val redeemedAt: Instant,
    val creditsAwarded: Int
)
