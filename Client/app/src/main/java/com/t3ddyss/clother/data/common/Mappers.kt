package com.t3ddyss.clother.data.common

import com.t3ddyss.clother.data.auth.db.models.UserEntity
import com.t3ddyss.clother.data.auth.remote.models.AuthDataDto
import com.t3ddyss.clother.data.auth.remote.models.UserDto
import com.t3ddyss.clother.data.chat.db.models.ChatEntity
import com.t3ddyss.clother.data.chat.db.models.ChatWithLastMessageEntity
import com.t3ddyss.clother.data.chat.db.models.MessageEntity
import com.t3ddyss.clother.data.chat.remote.models.ChatDto
import com.t3ddyss.clother.data.chat.remote.models.MessageDto
import com.t3ddyss.clother.data.common.remote.models.ResponseDto
import com.t3ddyss.clother.data.offers.db.models.CategoryEntity
import com.t3ddyss.clother.data.offers.db.models.OfferEntity
import com.t3ddyss.clother.data.offers.remote.models.OfferDto
import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.domain.common.models.Response
import com.t3ddyss.clother.domain.offers.models.Offer
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

    fun AuthDataDto.toDomain(): AuthData {
        return AuthData(
            user = this.user.toDomain(),
            accessToken = this.accessToken,
            refreshToken = this.refreshToken
        )
    }
}
