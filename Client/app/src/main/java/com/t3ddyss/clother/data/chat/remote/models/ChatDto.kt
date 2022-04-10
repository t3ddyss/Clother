package com.t3ddyss.clother.data.chat.remote.models

import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.data.auth.remote.models.UserDto

data class ChatDto(
    val id: Int,
    val interlocutor: UserDto,

    @SerializedName("last_message")
    val lastMessage: MessageDto
)
