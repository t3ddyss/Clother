package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.models.AuthResponse
import com.t3ddyss.clother.models.AuthTokens
import com.t3ddyss.clother.utilities.*
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

// TODO replace User with FieldMap
class UsersRepository @Inject constructor(
        private val service: ClotherAuthService,
        private val prefs: SharedPreferences
) {

    suspend fun createUser(user: User): ResponseState<AuthResponse> {
        return try {
            val response = service.createUserWithCredentials(user)
            Success(response.also { it.email = user.email })

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }

    suspend fun signInWithCredentials(user: User): ResponseState<AuthTokens> {
        return try {
            val response = service.signInWithCredentials(user)
            saveTokens(response)

            Success(response)

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }

    suspend fun resetPassword(user: User): ResponseState<AuthResponse> {
        return try {
            val response = service.resetPassword(user)
            Success(response.also { it.email = user.email })

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }

    private fun saveTokens(tokens: AuthTokens) {
        prefs.edit().putString(ACCESS_TOKEN, "Bearer ${tokens.accessToken}").apply()
        prefs.edit().putString(REFRESH_TOKEN, "Bearer ${tokens.refreshToken}").apply()
        prefs.edit().putBoolean(IS_AUTHENTICATED, true).apply()
    }
}