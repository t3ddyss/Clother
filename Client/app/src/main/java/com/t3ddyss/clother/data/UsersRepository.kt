package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.models.*
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import com.t3ddyss.clother.utilities.REFRESH_TOKEN
import com.t3ddyss.clother.utilities.handleError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

// TODO replace User with FieldMap
class UsersRepository @Inject constructor(
        private val service: ClotherAuthService,
        private val prefs: SharedPreferences
) {

    suspend fun createUser(user: User): ResponseState<SignUpResponse> {
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

    private fun saveTokens(response: AuthTokens) {
        prefs.edit().putString(ACCESS_TOKEN, response.accessToken).apply()
        prefs.edit().putString(REFRESH_TOKEN, response.refreshToken).apply()
        prefs.edit().putBoolean(IS_AUTHENTICATED, true).apply()
    }

    suspend fun resetPassword(user: User): ResponseState<PasswordResetResponse> {
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
}