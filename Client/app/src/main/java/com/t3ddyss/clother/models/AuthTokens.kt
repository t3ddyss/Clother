package com.t3ddyss.clother.models

import com.google.gson.annotations.SerializedName

data class AuthTokens(@SerializedName("access_token") val accessToken: String,
                      @SerializedName("refresh_token") val refreshToken: String)