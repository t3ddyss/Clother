package com.t3ddyss.clother.api

import com.t3ddyss.clother.data.SignInResponse
import com.t3ddyss.clother.data.SignUpResponse
import com.t3ddyss.clother.data.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ClotherService {

    @POST("register")
    suspend fun createUserWithCredentials(@Body user: User): SignUpResponse

    @POST("login")
    suspend fun signInWithCredentials(@Body user: User): SignInResponse

    @GET("error_test")
    suspend fun getUsers(): List<User>
}