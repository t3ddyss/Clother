package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.db.UserDao
import com.t3ddyss.clother.models.domain.Resource
import com.t3ddyss.clother.models.domain.Response
import com.t3ddyss.clother.models.domain.Success
import com.t3ddyss.clother.models.dto.AuthDataDto
import com.t3ddyss.clother.models.mappers.mapResponseDtoToDomain
import com.t3ddyss.clother.models.mappers.mapUserDtoToEntity
import com.t3ddyss.clother.utilities.*
import javax.inject.Inject


class UsersRepository @Inject constructor(
    private val service: ClotherAuthService,
    private val prefs: SharedPreferences,
    private val userDao: UserDao
) {
    suspend fun createUser(name: String, email: String, password: String):
            Resource<Response> {
        val user = mapOf("name" to name, "email" to email, "password" to password)

        return handleNetworkException {
            val response = service.createUserWithCredentials(user)
            Success(mapResponseDtoToDomain(response))
        }
    }

    suspend fun signInWithCredentials(email: String, password: String): Resource<AuthDataDto> {
        val user = mapOf("email" to email, "password" to password)

        return handleNetworkException {
            val response = service.signInWithCredentials(user)
            saveAuthData(response)
            Success(response)
        }
    }

    suspend fun resetPassword(email: String): Resource<Response> {
        return handleNetworkException {
            val response = service.resetPassword(mapOf("email" to email))
            Success(mapResponseDtoToDomain(response))
        }
    }

    private suspend fun saveAuthData(data: AuthDataDto) {
        prefs.edit().putString(ACCESS_TOKEN, "Bearer ${data.accessToken}").apply()
        prefs.edit().putString(REFRESH_TOKEN, "Bearer ${data.refreshToken}").apply()
        prefs.edit().putInt(CURRENT_USER_ID, data.user.id).apply()
        prefs.edit().putBoolean(IS_AUTHENTICATED, true).apply()

        userDao.insert(mapUserDtoToEntity(data.user))
    }
}