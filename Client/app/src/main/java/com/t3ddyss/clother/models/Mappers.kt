package com.t3ddyss.clother.models

import com.t3ddyss.clother.models.domain.*
import com.t3ddyss.clother.models.dto.*
import com.t3ddyss.clother.models.entity.*
import com.t3ddyss.core.domain.models.Category
import com.t3ddyss.core.domain.models.User

object Mappers {

    fun CategoryEntity.toDomain(): Category {
        return Category(
            id = this.id,
            title = this.title,
            isLastLevel = this.isLastLevel
        )
    }

    fun ChatDto.toEntity(): ChatEntity {
        return ChatEntity(
            serverId = this.id,
            interlocutor = this.interlocutor.toEntity()
        )
    }

    fun ChatWithLastMessageEntity.toDomain(): Chat {
        val isIncoming = this.chat.interlocutor.id == this.message.userId
        return Chat(
            id = this.chat.serverId ?: this.chat.localId,
            interlocutor = this.chat.interlocutor.toDomain(),
            lastMessage = this.message.toDomain(isIncoming)
        )
    }

    fun MessageDto.toEntity(): MessageEntity {
        return MessageEntity(
            serverId = this.id,
            serverChatId = this.chatId,
            userId = this.userId,
            createdAt = this.createdAt,
            body = this.body,
            image = this.image
        )
    }

    fun MessageDto.toDomain(isIncoming: Boolean): Message {
        return Message(
            localId = 0,
            serverId = this.id,
            userId = this.userId,
            userName = this.userName,
            createdAt = this.createdAt,
            status = MessageStatus.DELIVERED,
            body = this.body,
            image = this.image,
            isIncoming = isIncoming
        )
    }

    fun MessageEntity.toDomain(isIncoming: Boolean): Message {
        return Message(
            localId = this.localId,
            serverId = this.serverId,
            userId = this.userId,
            userName = "",
            createdAt = this.createdAt,
            status = this.status,
            body = this.body,
            image = this.image,
            isIncoming = isIncoming
        )
    }

    fun MessageEntity.toDto(): MessageDto {
        return MessageDto(
            id = this.localId,
            userId = this.userId,
            createdAt = this.createdAt,
            body = this.body,
            image = this.image,
            chatId = 0,
            userName = ""
        )
    }

    fun OfferDto.toDomain(): Offer {
        return Offer(
            id = this.id,
            userId = this.userId,
            userName = this.userName,
            createdAt = this.createdAt,
            title = this.title,
            description = this.description.orEmpty(),
            category = this.category,
            images = this.images,
            size = this.size.orEmpty(),
            location =this.location.orEmpty()
        )
    }

    fun OfferDto.toEntity(): OfferEntity {
        return OfferEntity(
            id = this.id,
            categoryId = this.categoryId,
            userId = this.userId,
            userName = this.userName,
            createdAt = this.createdAt,
            title = this.title,
            description = this.description.orEmpty(),
            category = this.category,
            images = this.images,
            size = this.size.orEmpty(),
            location = this.location.orEmpty()
        )
    }

    fun OfferEntity.toDomain(): Offer {
        return Offer(
            id = this.id,
            userId = this.userId,
            userName = this.userName,
            createdAt = this.createdAt,
            title = this.title,
            description = this.description.orEmpty(),
            category = this.category,
            images = this.images,
            size = this.size.orEmpty(),
            location = this.location.orEmpty()
        )
    }

    fun ResponseDto.toDomain(): Response {
        return Response(
            message = this.message.orEmpty()
        )
    }

    fun User.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            name = this.name,
            email = this.email,
            image = this.image
        )
    }

    fun UserDto.toDomain(): User {
        return User(
            id = this.id,
            name = this.name,
            email = this.email.orEmpty(),
            image = this.image.orEmpty()
        )
    }

    fun UserDto.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            name = this.name,
            email = this.email,
            image = this.image
        )
    }

    fun UserEntity.toDomain(): User {
        return User(
            id = this.id,
            name = this.name,
            email = this.email.orEmpty(),
            image = this.image.orEmpty()
        )
    }
}
