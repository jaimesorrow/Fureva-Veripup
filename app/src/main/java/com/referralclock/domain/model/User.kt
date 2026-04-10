package com.referralclock.domain.model

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val loyaltyCredits: Int = 0,
    val isVerified: Boolean = false
)
