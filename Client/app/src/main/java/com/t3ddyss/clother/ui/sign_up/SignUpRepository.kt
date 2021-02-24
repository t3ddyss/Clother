package com.t3ddyss.clother.ui.sign_up

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.api.*
import com.t3ddyss.clother.data.ErrorResponse
import com.t3ddyss.clother.data.SignUpResponse
import com.t3ddyss.clother.data.User
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException

// TODO inject RetrofitClient using Hilt or Dagger
class SignUpRepository {
    private val client: ClotherAuthService = RetrofitClient.instance

    suspend fun createUser(user: User): Resource<SignUpResponse> {
        return try {
            val response = client.createUserWithCredentials(user)
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