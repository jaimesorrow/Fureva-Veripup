package com.referralclock.presentation.dashboard.viewmodel

import com.referralclock.core.error.AppError
import com.referralclock.domain.model.Booking
import com.referralclock.domain.model.ReferralCode
import com.referralclock.domain.model.ReferralStats

// ── State ────────────────────────────────────────────────────────────────────
// No error fields — all errors are delivered via UiEffect.ShowSnackbar.
data class DashboardUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val loyaltyCredits: Int = 0,
    val stats: ReferralStats? = null,
    val recentCodes: List<ReferralCode> = emptyList(),
    val recentBookings: List<Booking> = emptyList()
)

// ── Events ───────────────────────────────────────────────────────────────────
// The UI may ONLY call onEvent(); no direct ViewModel method calls.
sealed class DashboardUiEvent {
    /** Triggered once by LaunchedEffect on first composition. */
    data object LoadDashboard : DashboardUiEvent()
    /** Pull-to-refresh or manual retry. */
    data object Refresh : DashboardUiEvent()
    /** User taps "Generate New Code". */
    data object GenerateCode : DashboardUiEvent()
    /** User taps the share icon next to an existing code. */
    data class ShareCode(val code: String) : DashboardUiEvent()
    /** User taps a booking row. */
    data class BookingClicked(val bookingId: String) : DashboardUiEvent()
    /** User taps "View All" in the referrals section. */
    data object ViewAllReferrals : DashboardUiEvent()
    /** User taps the new-booking FAB. */
    data object NewBooking : DashboardUiEvent()
}

// ── Effects ──────────────────────────────────────────────────────────────────
// One-shot side effects: share sheet, navigation, snackbar.
sealed class DashboardUiEffect {
    /** Open the Android share sheet for a referral code. */
    data class ShareCode(val codeString: String, val shareUrl: String) : DashboardUiEffect()
    /** All user-facing errors arrive here — never in UiState. */
    data class ShowSnackbar(val error: AppError) : DashboardUiEffect()
    data class NavigateToBooking(val bookingId: String) : DashboardUiEffect()
    data object NavigateToAllReferrals : DashboardUiEffect()
    data object NavigateToNewBooking : DashboardUiEffect()
}
