package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.domain.Message
import com.t3ddyss.clother.models.domain.MessageStatus
import com.t3ddyss.clother.models.dto.MessageDto
import com.t3ddyss.clother.models.entity.MessageEntity

fun mapMessageDtoToEntity(input: MessageDto): MessageEntity {
    return MessageEntity(
        serverId = input.id,
        serverChatId = input.chatId,
        userId = input.userId,
        createdAt = input.createdAt,
        body = input.body,
        image = input.image
    )
}

fun mapMessageDtoToDomain(input: MessageDto, isIncoming: Boolean): Message {
    return Message(
        localId = 0,
        serverId = input.id,
        userId = input.userId,
        userName = input.userName,
        createdAt = input.createdAt,
        status = MessageStatus.DELIVERED,
        body = input.body,
        image = input.image,
        isIncoming = isIncoming
    )
}

fun mapMessageEntityToDto(input: MessageEntity): MessageDto {
    return MessageDto(
        id = input.localId,
        userId = input.userId,
        createdAt = input.createdAt,
        body = input.body,
        image = input.image,
        chatId = 0,
        userName = ""
    )
}

fun mapMessageEntityToDomain(input: MessageEntity, isIncoming: Boolean): Message {
    return Message(
        localId = input.localId,
        serverId = input.serverId,
        userId = input.userId,
        userName = "",
        createdAt = input.createdAt,
        status = input.status,
        body = input.body,
        image = input.image,
        isIncoming = isIncoming
    )
}