package com.t3ddyss.clother.data

import com.google.gson.annotations.SerializedName

data class SignInResponse(@SerializedName("access_token") val accessToken: String,
                          @SerializedName("refresh_token") val refreshToken: String)