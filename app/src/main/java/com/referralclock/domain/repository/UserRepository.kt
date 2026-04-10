package com.referralclock.domain.repository

import com.referralclock.core.util.Result
import com.referralclock.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun currentUser(): Flow<User?>
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, displayName: String): Result<User>
    suspend fun signOut(): Result<Unit>
    suspend fun updateProfile(displayName: String): Result<User>
}
