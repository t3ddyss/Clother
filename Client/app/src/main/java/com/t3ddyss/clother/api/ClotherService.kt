package com.t3ddyss.clother.api

import com.t3ddyss.clother.data.ResponseSignUp
import com.t3ddyss.clother.data.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ClotherService {

    @POST("register")
    suspend fun createUserWithCredentials(@Body user: User): ResponseSignUp

    @GET("error_test")
    suspend fun getUsers(): List<User>
}