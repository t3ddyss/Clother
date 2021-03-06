package com.t3ddyss.clother.ui.sign_up

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.t3ddyss.clother.api.*
import com.t3ddyss.clother.data.*
import com.t3ddyss.clother.utilities.handleError
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class SignUpRepository @Inject constructor(private val authService: ClotherAuthService) {

    suspend fun createUser(user: User): ResponseState<SignUpResponse> {
        return try {
            val response = authService.createUserWithCredentials(user)
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