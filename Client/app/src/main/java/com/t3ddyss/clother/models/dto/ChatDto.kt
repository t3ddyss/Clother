package com.t3ddyss.clother.models.dto

import com.google.gson.annotations.SerializedName

data class ChatDto(
    val id: Int,
    val interlocutor: UserDto,

    @SerializedName("last_message")
    val lastMessage: MessageDto
)
