package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.domain.Chat
import com.t3ddyss.clother.models.dto.ChatDto
import com.t3ddyss.clother.models.entity.ChatEntity
import com.t3ddyss.clother.models.entity.ChatWithLastMessageEntity

fun mapChatDtoToEntity(input: ChatDto): ChatEntity {
    return ChatEntity(
        serverId = input.id,
        interlocutor = mapUserDtoToEntity(input.interlocutor)
    )
}

fun mapChatWithLastMessageEntityToDomain(input: ChatWithLastMessageEntity): Chat {
    return Chat(
        id = input.chat.serverId ?: input.chat.localId,
        interlocutor = mapUserEntityToDomain(input.chat.interlocutor),
        lastMessage = mapMessageEntityToDomain(
            input.message,
            input.chat.interlocutor.id == input.message.userId)
    )
}