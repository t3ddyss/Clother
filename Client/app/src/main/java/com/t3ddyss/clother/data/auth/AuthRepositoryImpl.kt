package com.t3ddyss.clother.data.auth

import androidx.room.withTransaction
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.domain.auth.AuthRepository
import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.auth.models.UserAuthData
import com.t3ddyss.clother.domain.common.common.models.Response
import com.t3ddyss.clother.util.toBearer
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

    override suspend fun signUp(name: String, email: String, password: String): Response {
        val user = mapOf("name" to name, "email" to email, "password" to password)
        return service.createUserWithCredentials(user).toDomain()
    }

    override suspend fun signIn(email: String, password: String): UserAuthData {
        val user = mapOf("email" to email, "password" to password)
        return service.signInWithCredentials(user).toDomain()
    }

    override suspend fun resetPassword(email: String): Response {
        val user = mapOf("email" to email)
        return service.resetPassword(user).toDomain()
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
}