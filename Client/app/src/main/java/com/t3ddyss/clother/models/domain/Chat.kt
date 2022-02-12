package com.t3ddyss.clother.models.domain

import com.t3ddyss.core.domain.models.User

data class Chat(
    val id: Int,
    val interlocutor: User,
    val lastMessage: Message
)
