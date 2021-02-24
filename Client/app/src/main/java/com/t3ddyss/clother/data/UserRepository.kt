package com.t3ddyss.clother.data

import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.api.RetrofitClient

class UserRepository {
    private val client: ClotherAuthService = RetrofitClient.instance

    suspend fun getUsers(): List<User> {
//        return client.getUsers()
        return listOf(User(email = "example@example.com"))
    }
}