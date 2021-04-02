package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.AuthTokens
import com.t3ddyss.clother.models.AuthResponse
import com.t3ddyss.clother.models.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ClotherAuthService {
    @GET("api/auth/refresh")
    suspend fun refreshTokens(@Header("Authorization") refreshToken: String): Response<AuthTokens>

    @POST("api/auth/register")
    suspend fun createUserWithCredentials(@Body user: User): AuthResponse

    @POST("api/auth/login")
    suspend fun signInWithCredentials(@Body user: User): AuthTokens

    @POST("api/auth/forgot_password")
    suspend fun resetPassword(@Body user: User): AuthResponse
}