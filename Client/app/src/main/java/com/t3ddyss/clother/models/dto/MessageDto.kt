package com.t3ddyss.clother.models.dto

import com.google.gson.annotations.SerializedName
import java.util.*

data class MessageDto(
    val id: Int,

    @SerializedName("chat_id")
    val chatId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("user_name")
    val userName: String,

    @SerializedName("created_at")
    val createdAt: Date,

    val body: String?,
    val image: String?
)