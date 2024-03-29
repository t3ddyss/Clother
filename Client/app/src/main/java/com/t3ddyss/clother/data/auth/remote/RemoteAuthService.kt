package com.t3ddyss.clother.data.auth.remote

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import com.t3ddyss.clother.data.auth.remote.models.ResetPasswordErrorDto
import com.t3ddyss.clother.data.auth.remote.models.SignInErrorDto
import com.t3ddyss.clother.data.auth.remote.models.SignUpErrorDto
import com.t3ddyss.clother.data.auth.remote.models.UserAuthDataDto
import com.t3ddyss.clother.data.auth.remote.models.UserDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface RemoteAuthService {
    @GET("api/auth/refresh")
    suspend fun refreshTokens(
        @Header("Authorization") refreshToken: String?
    ): Either<CallError, UserAuthDataDto>

    @POST("api/auth/register")
    suspend fun createUserWithCredentials(
        @Body user: Map<String, String>
    ): Either<SignUpErrorDto, Unit>

    @POST("api/auth/login")
    suspend fun signInWithCredentials(
        @Body user: Map<String, String>
    ): Either<SignInErrorDto, UserAuthDataDto>

    @POST("api/auth/forgot_password")
    suspend fun resetPassword(
        @Body user: Map<String, String>
    ): Either<ResetPasswordErrorDto, Unit>

    @POST("api/auth/device/{token}")
    suspend fun registerDevice(
        @Header("Authorization") accessToken: String?,
        @Path("token") token: String
    ): Either<CallError, Unit>

    @GET("api/users/{user_id}")
    suspend fun getUserDetails(
        @Header("Authorization") accessToken: String?,
        @Path("user_id") userId: Int
    ): Either<CallError, UserDto>

    @Multipart
    @POST("api/users/update")
    suspend fun updateUserDetails(
        @Header("Authorization") accessToken: String?,
        @Part("request") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): Either<CallError, UserDto>
}