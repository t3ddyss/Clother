package com.t3ddyss.clother.api

import com.t3ddyss.clother.data.User
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

interface ClotherService {

    @GET("error_test")
    suspend fun getUsers(): List<User>
}