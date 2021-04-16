package com.t3ddyss.clother.models.auth

import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.models.user.User

data class AuthData(
        val user: User,
        @SerializedName("access_token")
        val accessToken: String,
        @SerializedName("refresh_token")
        val refreshToken: String
)