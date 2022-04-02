package com.t3ddyss.clother.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AuthDataDto(
    val user: UserDto,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)