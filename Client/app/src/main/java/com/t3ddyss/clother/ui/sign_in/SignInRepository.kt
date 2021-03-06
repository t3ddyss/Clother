package com.t3ddyss.clother.ui.sign_in

import android.content.SharedPreferences
import com.t3ddyss.clother.api.*
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.IS_AUTHENTICATED
import com.t3ddyss.clother.utilities.REFRESH_TOKEN
import com.t3ddyss.clother.utilities.handleError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class SignInRepository @Inject constructor(
    private val authService: ClotherAuthService,
    private val prefs: SharedPreferences
) {

    suspend fun signInWithCredentials(user: User): ResponseState<SignInResponse> {
        return try {
            val response = authService.signInWithCredentials(user)
            saveTokens(response)

            Success(response)

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Failed()
        }
    }

    private fun saveTokens(response: SignInResponse) {
        prefs.edit().putString(ACCESS_TOKEN, response.accessToken).apply()
        prefs.edit().putString(REFRESH_TOKEN, response.refreshToken).apply()
        prefs.edit().putBoolean(IS_AUTHENTICATED, true).apply()
    }
}