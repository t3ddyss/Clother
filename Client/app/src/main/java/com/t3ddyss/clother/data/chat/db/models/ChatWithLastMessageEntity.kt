package com.t3ddyss.clother.data.chat.db.models

import androidx.room.Embedded
import com.t3ddyss.clother.data.auth.db.models.UserEntity

data class ChatWithLastMessageEntity(
    @Embedded(prefix = "chat_")
    val chat: ChatEntity,
    @Embedded(prefix = "interlocutor_")
    val interlocutor: UserEntity,
    @Embedded(prefix = "message_")
    val message: MessageEntity
)