package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.db.UserDao
import com.t3ddyss.clother.models.Mappers.toDomain
import com.t3ddyss.clother.models.Mappers.toEntity
import com.t3ddyss.clother.models.domain.AuthState
import com.t3ddyss.clother.models.domain.Response
import com.t3ddyss.clother.models.dto.AuthDataDto
import com.t3ddyss.clother.remote.RemoteAuthService
import com.t3ddyss.clother.util.*
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.Success
import javax.inject.Inject

class UsersRepository @Inject constructor(
    private val service: RemoteAuthService,
    private val userDao: UserDao,
    private val authStateObserver: AuthStateObserver,
    private val prefs: SharedPreferences
) {
    fun observeAuthState() = authStateObserver.authState

    suspend fun createUser(name: String, email: String, password: String):
            Resource<Response> {
        val user = mapOf("name" to name, "email" to email, "password" to password)

        return handleNetworkError {
            val response = service.createUserWithCredentials(user)
            Success(response.toDomain())
        }
    }

    suspend fun signInWithCredentials(email: String, password: String): Resource<AuthDataDto> {
        val user = mapOf("email" to email, "password" to password)

        return handleNetworkError {
            val response = service.signInWithCredentials(user)
            processAuthData(response)
            Success(response)
        }
    }

    suspend fun resetPassword(email: String): Resource<Response> {
        return handleNetworkError {
            val response = service.resetPassword(mapOf("email" to email))
            Success(response.toDomain())
        }
    }

    fun getCurrentUserId() = authStateObserver.currentUserId

    private suspend fun processAuthData(data: AuthDataDto) {
        prefs.edit().putString(ACCESS_TOKEN, data.accessToken.toBearer()).apply()
        prefs.edit().putString(REFRESH_TOKEN, data.refreshToken.toBearer()).apply()
        prefs.edit().putInt(CURRENT_USER_ID, data.user.id).apply()

        userDao.insert(data.user.toEntity())
        authStateObserver.authState.value = AuthState.Authenticated(
            data.accessToken.toBearer(),
            userId = data.user.id
        )
    }
}