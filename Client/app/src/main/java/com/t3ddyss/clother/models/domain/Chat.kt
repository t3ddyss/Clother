package com.t3ddyss.clother.models.domain

data class Chat(
    val id: Int,
    val interlocutor: User,
    val lastMessage: Message
)
