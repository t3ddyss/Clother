package com.t3ddyss.clother.data.auth.remote.models

import com.google.gson.annotations.SerializedName

data class UserAuthDataDto(
    val user: UserDto,

    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("refresh_token")
    val refreshToken: String
)