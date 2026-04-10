package com.referralclock.core.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.vector.ImageVector
import com.referralclock.R

enum class BottomNavItem(
    val screen: Screen,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    DASHBOARD(Screen.Dashboard, Icons.Default.Dashboard, R.string.nav_dashboard),
    CALENDAR(Screen.Calendar, Icons.Default.CalendarToday, R.string.nav_calendar),
    CODES(Screen.Codes, Icons.Default.QrCode, R.string.nav_codes),
    REFERRALS(Screen.Referrals, Icons.Default.Group, R.string.nav_referrals)
}
