package com.referralclock.core.error

sealed class AppError {
    data object NetworkError : AppError()
    data object Unauthorized : AppError()
    data object NotFound : AppError()
    data class RateLimitExceeded(val retryAfterSeconds: Int = 60) : AppError()
    data class CodeAlreadyRedeemed(val code: String) : AppError()
    data class InvalidCode(val code: String) : AppError()
    data object InsufficientCredits : AppError()
    data object EmailAlreadyInUse : AppError()
    data object WeakPassword : AppError()
    data object InvalidCredentials : AppError()
    data class Unknown(val message: String? = null) : AppError()
}
