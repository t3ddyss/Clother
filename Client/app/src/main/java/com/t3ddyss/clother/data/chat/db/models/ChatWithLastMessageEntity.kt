package com.t3ddyss.clother.data.chat.db.models

import androidx.room.Embedded

data class ChatWithLastMessageEntity(
    @Embedded(prefix = "chat_")
    val chat: ChatEntity,
    @Embedded(prefix = "message_")
    val message: MessageEntity
)