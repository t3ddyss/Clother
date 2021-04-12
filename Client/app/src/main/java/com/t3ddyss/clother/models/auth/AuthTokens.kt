package com.t3ddyss.clother.models.auth

import com.google.gson.annotations.SerializedName

data class AuthTokens(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String)