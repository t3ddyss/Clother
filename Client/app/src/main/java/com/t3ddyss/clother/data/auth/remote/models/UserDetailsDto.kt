package com.t3ddyss.clother.data.auth.remote.models

import com.google.gson.annotations.SerializedName
import java.util.*

data class UserDetailsDto(
    val email: String,

    @SerializedName("created_at")
    val createdAt: Date,

    val status: String?
)
