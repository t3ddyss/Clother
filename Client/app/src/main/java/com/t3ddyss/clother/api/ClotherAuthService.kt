package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.auth.AuthResponse
import com.t3ddyss.clother.models.auth.AuthData
import retrofit2.Response
import retrofit2.http.*

interface ClotherAuthService {
    @GET("api/auth/refresh")
    suspend fun refreshTokens(@Header("Authorization") refreshToken: String?): Response<AuthData>

    @POST("api/auth/register")
    suspend fun createUserWithCredentials(@Body user: Map<String, String>): AuthResponse

    @POST("api/auth/login")
    suspend fun signInWithCredentials(@Body user: Map<String, String>): AuthData

    @POST("api/auth/forgot_password")
    suspend fun resetPassword(@Body user: Map<String, String>): AuthResponse

    @POST("api/auth/device/{token}")
    suspend fun sendDeviceToken(@Header("Authorization") accessToken: String?,
                                @Path("token") token: String
    )
}