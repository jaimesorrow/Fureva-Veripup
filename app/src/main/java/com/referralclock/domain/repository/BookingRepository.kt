package com.referralclock.domain.repository

import com.referralclock.core.util.Result
import com.referralclock.domain.model.Booking
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    /** Reactive stream of upcoming (PENDING/CONFIRMED) bookings for the given user. */
    fun upcomingBookings(userId: String): Flow<List<Booking>>
    suspend fun getBookingById(bookingId: String): Result<Booking>
    suspend fun createBooking(
        clientId: String,
        scheduledAtEpochMillis: Long,
        durationMinutes: Int,
        referralCode: String? = null
    ): Result<Booking>
    suspend fun cancelBooking(bookingId: String): Result<Unit>
}
