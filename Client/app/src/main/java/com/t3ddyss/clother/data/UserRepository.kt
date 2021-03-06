package com.t3ddyss.clother.data

import com.t3ddyss.clother.api.ClotherAuthService

class UserRepository (private val authService: ClotherAuthService) {

    suspend fun getUsers(): List<User> {
//        return client.getUsers()
        return listOf(User(email = "example@example.com"))
    }
}