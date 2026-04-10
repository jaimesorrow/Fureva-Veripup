package com.referralclock.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.referralclock.core.deeplink.DeepLink

// NavGraph is intentionally ignorant of deep-link URLs.
// Deep-link resolution happens exclusively in DeepLinkHandler;
// NavGraph only receives typed DeepLink values.
@Composable
fun NavGraph(
    initialDeepLink: DeepLink? = null,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Auth composables added in Step 2
        // Dashboard added in Step 3
        // Calendar added in Step 4
        // Codes + Referrals added in Step 5
    }
}
