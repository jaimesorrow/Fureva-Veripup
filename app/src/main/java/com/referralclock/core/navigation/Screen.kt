package com.referralclock.core.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Dashboard : Screen("dashboard")
    data object Calendar : Screen("calendar")
    data object Codes : Screen("codes")
    data object Referrals : Screen("referrals")
    data object Profile : Screen("profile")
    data object Booking : Screen("booking/{bookingId}") {
        fun createRoute(bookingId: String) = "booking/$bookingId"
    }
}
