package com.t3ddyss.clother.data.auth.remote

import com.t3ddyss.clother.data.auth.remote.models.AuthDataDto
import com.t3ddyss.clother.data.common.common.remote.models.ResponseDto
import retrofit2.Response
import retrofit2.http.*

interface RemoteAuthService {
    @GET("api/auth/refresh")
    suspend fun refreshTokens(
        @Header("Authorization") refreshToken: String?
    ): Response<AuthDataDto>

    @POST("api/auth/register")
    suspend fun createUserWithCredentials(@Body user: Map<String, String>): ResponseDto

    @POST("api/auth/login")
    suspend fun signInWithCredentials(@Body user: Map<String, String>): AuthDataDto

    @POST("api/auth/forgot_password")
    suspend fun resetPassword(@Body user: Map<String, String>): ResponseDto

    @POST("api/auth/device/{token}")
    suspend fun sendDeviceToken(
        @Header("Authorization") accessToken: String?,
        @Path("token") token: String
    )
}