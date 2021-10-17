package com.t3ddyss.clother.models.entity

import androidx.room.Embedded

data class ChatWithLastMessageEntity(
    @Embedded(prefix = "chat_")
    val chat: ChatEntity,
    @Embedded(prefix = "message_")
    val message: MessageEntity
)