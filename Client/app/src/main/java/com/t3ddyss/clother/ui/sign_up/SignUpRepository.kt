package com.t3ddyss.clother.ui.sign_up

import com.t3ddyss.clother.api.ClotherService
import com.t3ddyss.clother.api.RetrofitClient
import com.t3ddyss.clother.data.ResponseSignUp
import com.t3ddyss.clother.data.User

class SignUpRepository {
    private val client: ClotherService = RetrofitClient.instance

    suspend fun createUser(user: User): ResponseSignUp {
        return client.createUserWithCredentials(user)
    }
}