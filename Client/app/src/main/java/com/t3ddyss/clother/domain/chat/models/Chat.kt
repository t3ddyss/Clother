package com.t3ddyss.clother.domain.chat.models

import com.t3ddyss.clother.domain.auth.models.User

data class Chat(
    val id: Int,
    val interlocutor: User,
    val lastMessage: Message
)
