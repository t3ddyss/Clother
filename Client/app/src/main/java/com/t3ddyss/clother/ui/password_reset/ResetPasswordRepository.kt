package com.t3ddyss.clother.ui.password_reset

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.api.*
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.utilities.handleError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class ResetPasswordRepository @Inject constructor(private val authService: ClotherAuthService){

    suspend fun resetPassword(user: User): ResponseState<PasswordResetResponse> {
        return try {
            val response = authService.resetPassword(user)
            Success(response.also { it.email = user.email })

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Failed()
        }
    }
}