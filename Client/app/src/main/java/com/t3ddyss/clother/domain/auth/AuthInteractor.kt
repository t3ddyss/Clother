package com.t3ddyss.clother.domain.auth

import arrow.core.Either
import arrow.core.Nel
import com.t3ddyss.clother.domain.auth.models.*
import kotlinx.coroutines.flow.StateFlow

interface AuthInteractor {
    val authStateFlow: StateFlow<AuthState>
    fun initialize()
    suspend fun signUp(name: String, email: String, password: String): Either<SignUpError, Unit>
    suspend fun signIn(email: String, password: String): Either<SignInError, UserAuthData>
    suspend fun resetPassword(email: String): Either<ResetPasswordError, Unit>
    suspend fun validateParameters(
        name: String? = null,
        email: String? = null,
        password: String? = null
    ): Either<Nel<AuthParam>, Unit>

    enum class AuthParam {
        NAME,
        EMAIL,
        PASSWORD
    }
}