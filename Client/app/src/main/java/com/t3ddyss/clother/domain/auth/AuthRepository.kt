package com.t3ddyss.clother.domain.auth

import arrow.core.Either
import com.t3ddyss.clother.domain.auth.models.*

interface AuthRepository {
    val authData: AuthData
    suspend fun signUp(name: String, email: String, password: String): Either<SignUpError, Unit>
    suspend fun signIn(email: String, password: String): Either<SignInError, UserAuthData>
    suspend fun resetPassword(email: String): Either<ResetPasswordError, Unit>
    suspend fun saveUserAuthData(userAuthData: UserAuthData)
    suspend fun deleteAllUserData()
}