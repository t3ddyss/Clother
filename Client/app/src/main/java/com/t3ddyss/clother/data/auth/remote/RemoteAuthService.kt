package com.t3ddyss.clother.data.auth.remote

import com.t3ddyss.clother.data.auth.remote.models.UserAuthDataDto
import com.t3ddyss.clother.data.auth.remote.models.UserDto
import com.t3ddyss.clother.data.common.common.remote.models.ResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface RemoteAuthService {
    @GET("api/auth/refresh")
    suspend fun refreshTokens(
        @Header("Authorization") refreshToken: String?
    ): Response<UserAuthDataDto>

    @POST("api/auth/register")
    suspend fun createUserWithCredentials(@Body user: Map<String, String>): ResponseDto

    @POST("api/auth/login")
    suspend fun signInWithCredentials(@Body user: Map<String, String>): UserAuthDataDto

    @POST("api/auth/forgot_password")
    suspend fun resetPassword(@Body user: Map<String, String>): ResponseDto

    @POST("api/auth/device/{token}")
    suspend fun sendDeviceToken(
        @Header("Authorization") accessToken: String?,
        @Path("token") token: String
    )

    @GET("api/users/{user_id}")
    suspend fun getUserDetails(
        @Header("Authorization") accessToken: String?,
        @Path("user_id") userId: Int
    ): UserDto

    @Multipart
    @POST("api/users/update")
    suspend fun updateUserDetails(
        @Header("Authorization") accessToken: String?,
        @Part("request") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): UserDto
}