package com.referralclock.presentation.dashboard.viewmodel

import androidx.lifecycle.viewModelScope
import com.referralclock.core.mvi.BaseViewModel
import com.referralclock.core.util.Result
import com.referralclock.domain.repository.BookingRepository
import com.referralclock.domain.repository.ReferralRepository
import com.referralclock.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// FirebaseAuth is NOT imported here — only UserRepository is used.
private const val SHARE_BASE_URL = "https://referralclock.app/join?code="

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val bookingRepository: BookingRepository,
    private val referralRepository: ReferralRepository
) : BaseViewModel<DashboardUiState, DashboardUiEvent, DashboardUiEffect>(DashboardUiState()) {

    private var loadJob: Job? = null

    init {
        observeUserProfile()
        observeUpcomingBookings()
    }

    override fun onEvent(event: DashboardUiEvent) {
        when (event) {
            DashboardUiEvent.LoadDashboard,
            DashboardUiEvent.Refresh -> loadDashboard()

            DashboardUiEvent.GenerateCode -> generateCode()

            is DashboardUiEvent.ShareCode -> emitEffect(
                DashboardUiEffect.ShareCode(
                    codeString = event.code,
                    shareUrl = "$SHARE_BASE_URL${event.code}"
                )
            )

            is DashboardUiEvent.BookingClicked ->
                emitEffect(DashboardUiEffect.NavigateToBooking(event.bookingId))

            DashboardUiEvent.ViewAllReferrals ->
                emitEffect(DashboardUiEffect.NavigateToAllReferrals)

            DashboardUiEvent.NewBooking ->
                emitEffect(DashboardUiEffect.NavigateToNewBooking)
        }
    }

    // ── Reactive observers ────────────────────────────────────────────────────

    /** Keeps userName and loyaltyCredits in sync with Firestore in real time. */
    private fun observeUserProfile() {
        viewModelScope.launch {
            userRepository.currentUser()
                .filterNotNull()
                .collectLatest { user ->
                    setState {
                        copy(
                            userName = user.displayName,
                            loyaltyCredits = user.loyaltyCredits
                        )
                    }
                }
        }
    }

    /** Upcoming bookings flow switches automatically when the current user changes. */
    private fun observeUpcomingBookings() {
        viewModelScope.launch {
            userRepository.currentUser()
                .filterNotNull()
                .flatMapLatest { user -> bookingRepository.upcomingBookings(user.id) }
                .collect { bookings -> setState { copy(recentBookings = bookings) } }
        }
    }

    // ── One-shot loaders ──────────────────────────────────────────────────────

    /**
     * Loads stats and recent codes in parallel.
     * Cancels any in-flight load so rapid Refresh taps never race.
     */
    private fun loadDashboard() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            setState { copy(isLoading = true) }
            val user = userRepository.currentUser().filterNotNull().first()

            val statsDeferred = async { referralRepository.getStats(user.id) }
            val codesDeferred = async { referralRepository.getRecentCodes(user.id) }

            val statsResult = statsDeferred.await()
            val codesResult = codesDeferred.await()

            // Errors go to snackbar — never into UiState.
            if (statsResult is Result.Error) {
                emitEffect(DashboardUiEffect.ShowSnackbar(statsResult.error))
            }
            if (codesResult is Result.Error) {
                emitEffect(DashboardUiEffect.ShowSnackbar(codesResult.error))
            }

            setState {
                copy(
                    isLoading = false,
                    stats = (statsResult as? Result.Success)?.data ?: stats,
                    recentCodes = (codesResult as? Result.Success)?.data ?: recentCodes
                )
            }
        }
    }

    /**
     * Asks the repository to generate a new referral code, then immediately
     * emits a ShareCode effect so the Android share sheet opens automatically.
     */
    private fun generateCode() {
        viewModelScope.launch {
            val user = userRepository.currentUser().filterNotNull().first()
            when (val result = referralRepository.generateCode(user.id)) {
                is Result.Success -> {
                    // Prepend the freshly generated code to the list.
                    setState { copy(recentCodes = listOf(result.data) + recentCodes) }
                    emitEffect(
                        DashboardUiEffect.ShareCode(
                            codeString = result.data.code,
                            shareUrl = "$SHARE_BASE_URL${result.data.code}"
                        )
                    )
                }
                is Result.Error -> emitEffect(DashboardUiEffect.ShowSnackbar(result.error))
            }
        }
    }
}
