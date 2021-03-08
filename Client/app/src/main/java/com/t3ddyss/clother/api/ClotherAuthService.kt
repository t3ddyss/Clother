package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.PasswordResetResponse
import com.t3ddyss.clother.models.SignInResponse
import com.t3ddyss.clother.models.SignUpResponse
import com.t3ddyss.clother.models.User
import retrofit2.http.Body
import retrofit2.http.POST

interface ClotherAuthService {
    @POST("register")
    suspend fun createUserWithCredentials(@Body user: User): SignUpResponse

    @POST("login")
    suspend fun signInWithCredentials(@Body user: User): SignInResponse

    @POST("auth/forgot_password")
    suspend fun resetPassword(@Body user: User): PasswordResetResponse
}