package com.t3ddyss.clother.data.auth

import com.t3ddyss.clother.data.Mappers.toDomain
import com.t3ddyss.clother.data.Mappers.toEntity
import com.t3ddyss.clother.data.Storage
import com.t3ddyss.clother.data.db.UserDao
import com.t3ddyss.clother.data.remote.RemoteAuthService
import com.t3ddyss.clother.domain.auth.AuthRepository
import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.models.Response
import com.t3ddyss.clother.util.toBearer
import com.t3ddyss.core.domain.models.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val service: RemoteAuthService,
    private val userDao: UserDao,
    private val storage: Storage
) : AuthRepository {

    override val authData: AuthData
        get() {
            return AuthData(
                User(storage.userId),
                storage.accessToken,
                storage.refreshToken
            )
        }

    override suspend fun signUp(name: String, email: String, password: String): Response {
        val user = mapOf("name" to name, "email" to email, "password" to password)
        return service.createUserWithCredentials(user).toDomain()
    }

    override suspend fun signIn(email: String, password: String): AuthData {
        val user = mapOf("email" to email, "password" to password)
        return service.signInWithCredentials(user).toDomain()
    }

    override suspend fun resetPassword(email: String): Response {
        val user = mapOf("email" to email)
        return service.resetPassword(user).toDomain()
    }

    override suspend fun saveAuthData(authData: AuthData) {
        storage.accessToken = authData.accessToken?.toBearer()
        storage.refreshToken = authData.refreshToken?.toBearer()
        storage.userId = authData.user.id
        userDao.insert(authData.user.toEntity())
    }

    override suspend fun deleteAuthData() {
        storage.accessToken = null
        storage.refreshToken = null
        storage.userId = 0
        userDao.deleteAll()
    }
}