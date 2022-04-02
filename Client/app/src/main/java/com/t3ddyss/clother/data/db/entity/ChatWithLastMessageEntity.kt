package com.t3ddyss.clother.data.db.entity

import androidx.room.Embedded

data class ChatWithLastMessageEntity(
    @Embedded(prefix = "chat_")
    val chat: ChatEntity,
    @Embedded(prefix = "message_")
    val message: MessageEntity
)