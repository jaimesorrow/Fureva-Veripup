package com.referralclock.domain.model

import java.time.Instant

data class Booking(
    val id: String,
    val clientId: String,
    val clientName: String,
    val scheduledAt: Instant,
    val durationMinutes: Int,
    val status: BookingStatus,
    val referralCodeUsed: String? = null
)

enum class BookingStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED
}
