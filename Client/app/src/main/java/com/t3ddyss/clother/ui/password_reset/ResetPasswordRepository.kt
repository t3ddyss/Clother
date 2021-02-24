package com.t3ddyss.clother.ui.password_reset

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.api.*
import com.t3ddyss.clother.data.ErrorResponse
import com.t3ddyss.clother.data.PasswordResetResponse
import com.t3ddyss.clother.data.User
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

// TODO inject RetrofitClient using Hilt or Dagger
class ResetPasswordRepository {
    private val client: ClotherAuthService = RetrofitClient.instance

    suspend fun resetPassword(user: User): Resource<PasswordResetResponse> {
        return try {
            val response = client.resetPassword(user)
            Success(response.also { it.email = user.email })

        } catch (ex: HttpException) {
            val gson = Gson()
            val type = object : TypeToken<ErrorResponse>() {}.type
            val response: ErrorResponse? = gson
                .fromJson(ex.response()?.errorBody()?.charStream(), type)
            Error(response?.message)

        } catch (ex: ConnectException) {
            Failed()
        } catch (ex: SocketTimeoutException) {
            Failed()
        }
    }
}