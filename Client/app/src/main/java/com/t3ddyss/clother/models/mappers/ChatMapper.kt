package com.t3ddyss.clother.models.mappers

import com.t3ddyss.clother.models.dto.ChatDto

fun mapChatDtoToEntity(input: ChatDto): com.t3ddyss.clother.models.entity.ChatEntity {
    return com.t3ddyss.clother.models.entity.ChatEntity(
        serverId = input.id,
        interlocutor = mapUserDtoToEntity(input.interlocutor)
    )
}