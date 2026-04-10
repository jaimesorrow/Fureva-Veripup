package com.referralclock.core.error

import androidx.annotation.StringRes
import com.referralclock.R

@StringRes
fun AppError.toStringRes(): Int = when (this) {
    AppError.NetworkError -> R.string.error_network
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.NotFound -> R.string.error_not_found
    is AppError.RateLimitExceeded -> R.string.error_rate_limit
    is AppError.CodeAlreadyRedeemed -> R.string.error_code_already_redeemed
    is AppError.InvalidCode -> R.string.error_invalid_code
    AppError.InsufficientCredits -> R.string.error_insufficient_credits
    AppError.EmailAlreadyInUse -> R.string.error_email_in_use
    AppError.WeakPassword -> R.string.error_weak_password
    AppError.InvalidCredentials -> R.string.error_invalid_credentials
    is AppError.Unknown -> R.string.error_unknown
}
