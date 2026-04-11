package com.referralclock.presentation.dashboard.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.referralclock.R
import com.referralclock.core.error.toStringRes
import com.referralclock.domain.model.Booking
import com.referralclock.domain.model.ReferralCode
import com.referralclock.domain.model.ReferralStats
import com.referralclock.presentation.dashboard.viewmodel.DashboardUiEffect
import com.referralclock.presentation.dashboard.viewmodel.DashboardUiEvent
import com.referralclock.presentation.dashboard.viewmodel.DashboardUiState
import com.referralclock.presentation.dashboard.viewmodel.DashboardViewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// ── Entry point ───────────────────────────────────────────────────────────────
// The screen only (1) collects uiState, (2) collects effects, (3) calls onEvent().
// No direct ViewModel method calls are permitted.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToBooking: (String) -> Unit,
    onNavigateToAllReferrals: () -> Unit,
    onNavigateToNewBooking: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // ── Initial load ──────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.onEvent(DashboardUiEvent.LoadDashboard)
    }

    // ── Effect handler ────────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DashboardUiEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(context.getString(effect.error.toStringRes()))

                is DashboardUiEffect.ShareCode -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, effect.shareUrl)
                    }
                    context.startActivity(Intent.createChooser(intent, null))
                }

                is DashboardUiEffect.NavigateToBooking ->
                    onNavigateToBooking(effect.bookingId)

                DashboardUiEffect.NavigateToAllReferrals ->
                    onNavigateToAllReferrals()

                DashboardUiEffect.NavigateToNewBooking ->
                    onNavigateToNewBooking()
            }
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.dashboard_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(DashboardUiEvent.NewBooking) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.dashboard_booking_new_cd)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            DashboardContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

// ── Content ───────────────────────────────────────────────────────────────────
@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onEvent: (DashboardUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            WelcomeCard(
                userName = uiState.userName,
                loyaltyCredits = uiState.loyaltyCredits
            )
        }

        uiState.stats?.let { stats ->
            item { StatsCard(stats = stats) }
        }

        item {
            ReferralCodesCard(
                codes = uiState.recentCodes,
                onGenerate = { onEvent(DashboardUiEvent.GenerateCode) },
                onShare = { code -> onEvent(DashboardUiEvent.ShareCode(code)) }
            )
        }

        item {
            UpcomingBookingsCard(
                bookings = uiState.recentBookings,
                onBookingClick = { id -> onEvent(DashboardUiEvent.BookingClicked(id)) },
                onViewAllReferrals = { onEvent(DashboardUiEvent.ViewAllReferrals) }
            )
        }
    }
}

// ── Cards ─────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeCard(userName: String, loyaltyCredits: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_welcome, userName),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dashboard_loyalty_credits, loyaltyCredits),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatsCard(stats: ReferralStats) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_stats_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    label = stringResource(R.string.dashboard_stat_referrals),
                    value = stats.totalReferrals.toString()
                )
                StatItem(
                    label = stringResource(R.string.dashboard_stat_credits),
                    value = stats.totalCreditsEarned.toString()
                )
                StatItem(
                    label = stringResource(R.string.dashboard_stat_bookings),
                    value = stats.totalBookingsFromReferrals.toString()
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ReferralCodesCard(
    codes: List<ReferralCode>,
    onGenerate: () -> Unit,
    onShare: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.dashboard_code_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))

            if (codes.isEmpty()) {
                Text(
                    text = stringResource(R.string.dashboard_code_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                codes.forEach { referralCode ->
                    ReferralCodeRow(
                        referralCode = referralCode,
                        onShare = { onShare(referralCode.code) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onGenerate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.dashboard_code_generate))
            }
        }
    }
}

@Composable
private fun ReferralCodeRow(
    referralCode: ReferralCode,
    onShare: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = referralCode.code,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (referralCode.isActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = stringResource(R.string.dashboard_code_redeemed, referralCode.timesRedeemed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (referralCode.isActive) {
            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.dashboard_code_share_cd)
                )
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun UpcomingBookingsCard(
    bookings: List<Booking>,
    onBookingClick: (String) -> Unit,
    onViewAllReferrals: () -> Unit
) {
    // Formatter is stable within a composition; recreating only when locale changes.
    val formatter = remember {
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withZone(ZoneId.systemDefault())
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_bookings_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewAllReferrals) {
                    Text(stringResource(R.string.dashboard_view_all))
                }
            }

            if (bookings.isEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.dashboard_bookings_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                bookings.forEach { booking ->
                    BookingRow(
                        booking = booking,
                        formattedTime = formatter.format(booking.scheduledAt),
                        onClick = { onBookingClick(booking.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingRow(
    booking: Booking,
    formattedTime: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = booking.clientName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
