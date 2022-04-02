package com.t3ddyss.clother.data.auth

import android.content.SharedPreferences
import com.t3ddyss.clother.data.Mappers.toDomain
import com.t3ddyss.clother.data.Mappers.toEntity
import com.t3ddyss.clother.data.db.UserDao
import com.t3ddyss.clother.data.remote.RemoteAuthService
import com.t3ddyss.clother.domain.auth.AuthRepository
import com.t3ddyss.clother.domain.models.AuthData
import com.t3ddyss.clother.domain.models.Response
import com.t3ddyss.clother.util.ACCESS_TOKEN
import com.t3ddyss.clother.util.CURRENT_USER_ID
import com.t3ddyss.clother.util.REFRESH_TOKEN
import com.t3ddyss.clother.util.toBearer
import com.t3ddyss.core.domain.models.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val service: RemoteAuthService,
    private val userDao: UserDao,
    private val prefs: SharedPreferences
) : AuthRepository {

    override val authData: AuthData
        get() {
            val userId = prefs.getInt(CURRENT_USER_ID, 0)
            val accessToken = prefs.getString(ACCESS_TOKEN, null)
            val refreshToken = prefs.getString(REFRESH_TOKEN, null)
            return AuthData(User(userId), accessToken, refreshToken)
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
        prefs.edit().putString(ACCESS_TOKEN, authData.accessToken?.toBearer()).apply()
        prefs.edit().putString(REFRESH_TOKEN, authData.refreshToken?.toBearer()).apply()
        prefs.edit().putInt(CURRENT_USER_ID, authData.user.id).apply()
        userDao.insert(authData.user.toEntity())
    }

    override suspend fun deleteAuthData() {
        prefs.edit().remove(ACCESS_TOKEN).apply()
        prefs.edit().remove(REFRESH_TOKEN).apply()
        prefs.edit().remove(CURRENT_USER_ID).apply()
        userDao.deleteAll()
    }
}