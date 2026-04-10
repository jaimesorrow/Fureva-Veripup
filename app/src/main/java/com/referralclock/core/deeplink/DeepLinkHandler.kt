package com.referralclock.core.deeplink

import android.content.Intent
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

sealed class DeepLink {
    data class JoinWithCode(val code: String) : DeepLink()
    data class ViewBooking(val bookingId: String) : DeepLink()
    data object Dashboard : DeepLink()
    data object Unknown : DeepLink()
}

@Singleton
class DeepLinkHandler @Inject constructor() {

    fun resolve(intent: Intent): DeepLink? {
        val uri = intent.data ?: return null
        return resolve(uri)
    }

    fun resolve(uri: Uri): DeepLink {
        return when {
            uri.path?.startsWith("/join") == true -> {
                val code = uri.getQueryParameter("code") ?: return DeepLink.Unknown
                DeepLink.JoinWithCode(code)
            }
            uri.path?.startsWith("/booking") == true -> {
                val id = uri.getQueryParameter("id") ?: return DeepLink.Unknown
                DeepLink.ViewBooking(id)
            }
            uri.path?.startsWith("/dashboard") == true -> DeepLink.Dashboard
            else -> DeepLink.Unknown
        }
    }
}
