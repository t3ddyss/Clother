package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.db.UserDao
import com.t3ddyss.clother.models.auth.AuthResponse
import com.t3ddyss.clother.models.auth.AuthData
import com.t3ddyss.clother.models.common.Error
import com.t3ddyss.clother.models.common.Failed
import com.t3ddyss.clother.models.common.Resource
import com.t3ddyss.clother.models.common.Success
import com.t3ddyss.clother.models.user.User
import com.t3ddyss.clother.utilities.*
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject


class UsersRepository @Inject constructor(
        private val service: ClotherAuthService,
        private val prefs: SharedPreferences,
        private val userDao: UserDao
) {
    // TODO use generic function for handling exceptions
    suspend fun createUser(name: String, email: String, password: String):
            Resource<AuthResponse> {
        val user = mapOf("name" to name, "email" to email, "password" to password)
        return try {
            val response = service.createUserWithCredentials(user)
            Success(response.also { it.email = email })

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }

    suspend fun signInWithCredentials(email: String, password: String): Resource<AuthData> {
        val user = mapOf("email" to email, "password" to password)
        return try {
            val response = service.signInWithCredentials(user)
            saveAuthData(response)

            Success(response)

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }

    suspend fun resetPassword(email: String): Resource<AuthResponse> {
        return try {
            val response = service.resetPassword(mapOf("email" to email))
            Success(response.also { it.email = email })

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }

    suspend fun getCurrentUser(): User {
        return userDao.getCurrentUser(prefs.getInt(USER_ID, 0))
    }

    private suspend fun saveAuthData(data: AuthData) {
        prefs.edit().putString(ACCESS_TOKEN, "Bearer ${data.accessToken}").apply()
        prefs.edit().putString(REFRESH_TOKEN, "Bearer ${data.refreshToken}").apply()
        prefs.edit().putInt(USER_ID, data.user.id).apply()
        prefs.edit().putBoolean(IS_AUTHENTICATED, true).apply()

        userDao.insert(data.user)
    }
}