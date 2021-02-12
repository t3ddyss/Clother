package com.t3ddyss.clother.data

import android.util.Log
import com.t3ddyss.clother.api.ClotherService
import com.t3ddyss.clother.api.RetrofitClient

class UserRepository {
    private val client: ClotherService? = RetrofitClient.instance

    suspend fun getUsers():List<User>? {
        return client?.getUsers()
    }
}