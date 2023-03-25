package com.t3ddyss.clother.data.auth

import androidx.room.withTransaction
import arrow.core.Either
import arrow.core.flatten
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.auth.remote.models.ResetPasswordErrorDto
import com.t3ddyss.clother.data.auth.remote.models.ResetPasswordErrorType
import com.t3ddyss.clother.data.auth.remote.models.SignInErrorDto
import com.t3ddyss.clother.data.auth.remote.models.SignInErrorType
import com.t3ddyss.clother.data.auth.remote.models.SignUpErrorDto
import com.t3ddyss.clother.data.auth.remote.models.SignUpErrorType
import com.t3ddyss.clother.data.common.common.Mappers.toApiCallError
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.domain.auth.AuthRepository
import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.auth.models.ResetPasswordError
import com.t3ddyss.clother.domain.auth.models.SignInError
import com.t3ddyss.clother.domain.auth.models.SignUpError
import com.t3ddyss.clother.domain.auth.models.UserAuthData
import com.t3ddyss.clother.util.extensions.toBearer
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val service: RemoteAuthService,
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val storage: Storage
) : AuthRepository {

    override val authData: AuthData
        get() {
            return AuthData(
                storage.userId,
                storage.accessToken,
                storage.refreshToken
            )
        }

    override suspend fun signUp(name: String, email: String, password: String): Either<SignUpError, Unit> {
        val user = mapOf("name" to name, "email" to email, "password" to password)
        return Either.catch(
            { SignUpError.Common(it.toApiCallError()) },
            { service.createUserWithCredentials(user).mapLeft { it.toDomain() } }
        ).flatten()
    }

    override suspend fun signIn(email: String, password: String): Either<SignInError, UserAuthData> {
        val user = mapOf("email" to email, "password" to password)
        return Either.catch(
            { SignInError.Common(it.toApiCallError()) },
            { service.signInWithCredentials(user).map { it.toDomain() }.mapLeft { it.toDomain() } }
        ).flatten()
    }

    override suspend fun resetPassword(email: String): Either<ResetPasswordError, Unit> {
        val user = mapOf("email" to email)
        return Either.catch(
            { ResetPasswordError.Common(it.toApiCallError()) },
            { service.resetPassword(user).mapLeft { it.toDomain() }}
        ).flatten()
    }

    override suspend fun saveUserAuthData(userAuthData: UserAuthData) {
        val user = userAuthData.user
        storage.accessToken = userAuthData.accessToken.toBearer()
        storage.refreshToken = userAuthData.refreshToken.toBearer()
        storage.userId = user.id

        db.withTransaction {
            val userId = userDao.insert(user.toEntity())
            user.details?.let {
                userDao.insert(user.details.toEntity(userId.toInt()))
            }
        }
    }

    // TODO create separate prefs for auth data
    override suspend fun deleteAllUserData() {
        val isOnboardingCompleted = storage.isOnboardingCompleted
        storage.clear()
        storage.isOnboardingCompleted = isOnboardingCompleted
        userDao.deleteAll()
    }

    private fun SignUpErrorDto.toDomain() = when (type) {
        SignUpErrorType.EMAIL_OCCUPIED -> SignUpError.EmailOccupied
    }

    private fun SignInErrorDto.toDomain() = when (type) {
        SignInErrorType.EMAIL_NOT_VERIFIED -> SignInError.EmailNotVerified
        SignInErrorType.INVALID_CREDENTIALS -> SignInError.InvalidCredentials
    }

    private fun ResetPasswordErrorDto.toDomain() = when (type) {
        ResetPasswordErrorType.USER_NOT_FOUND -> ResetPasswordError.UserNotFound
    }
}