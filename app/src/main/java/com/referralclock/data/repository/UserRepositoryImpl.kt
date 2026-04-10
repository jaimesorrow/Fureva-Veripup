package com.referralclock.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.referralclock.core.error.AppError
import com.referralclock.core.util.Result
import com.referralclock.domain.model.User
import com.referralclock.domain.repository.UserRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// FirebaseAuth is allowed ONLY in this file — never in any ViewModel.
class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun currentUser(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String): Result<User> = try {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: return Result.Error(AppError.Unknown())
        Result.Success(user.toDomain())
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        Result.Error(AppError.InvalidCredentials)
    } catch (e: Exception) {
        Result.Error(AppError.Unknown(e.localizedMessage))
    }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String
    ): Result<User> = try {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val fbUser = result.user ?: return Result.Error(AppError.Unknown())
        fbUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
        ).await()
        Result.Success(fbUser.toDomain(displayNameOverride = displayName))
    } catch (e: FirebaseAuthUserCollisionException) {
        Result.Error(AppError.EmailAlreadyInUse)
    } catch (e: FirebaseAuthWeakPasswordException) {
        Result.Error(AppError.WeakPassword)
    } catch (e: Exception) {
        Result.Error(AppError.Unknown(e.localizedMessage))
    }

    override suspend fun signOut(): Result<Unit> = try {
        auth.signOut()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.Unknown(e.localizedMessage))
    }

    override suspend fun updateProfile(displayName: String): Result<User> = try {
        val fbUser = auth.currentUser ?: return Result.Error(AppError.Unauthorized)
        fbUser.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
        ).await()
        Result.Success(fbUser.toDomain(displayNameOverride = displayName))
    } catch (e: Exception) {
        Result.Error(AppError.Unknown(e.localizedMessage))
    }

    private fun FirebaseUser.toDomain(displayNameOverride: String? = null) = User(
        id = uid,
        displayName = displayNameOverride ?: displayName.orEmpty(),
        email = email.orEmpty(),
        isVerified = isEmailVerified
    )
}
