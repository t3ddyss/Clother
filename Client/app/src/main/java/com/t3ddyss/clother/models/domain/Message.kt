package com.t3ddyss.clother.models.domain

import java.util.*

data class Message(
    val id: Int,
    val chatId: Int,
    val userId: Int,
    val userName: String,
    val createdAt: Date,
    var status: MessageStatus,
    val body: String?,
    val image: String?
)
