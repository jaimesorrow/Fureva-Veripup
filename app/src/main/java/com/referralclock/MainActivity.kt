package com.referralclock

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.referralclock.core.deeplink.DeepLinkHandler
import com.referralclock.core.navigation.NavGraph
import com.referralclock.core.ui.theme.ReferralClockTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var deepLinkHandler: DeepLinkHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialDeepLink = deepLinkHandler.resolve(intent)
        setContent {
            ReferralClockTheme {
                NavGraph(initialDeepLink = initialDeepLink)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Deep links when app is already running are handled via
        // a shared StateFlow injected into NavGraph in Step 3+.
        setIntent(intent)
    }
}
