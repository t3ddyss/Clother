package com.t3ddyss.clother.data.common.common

import com.t3ddyss.clother.data.auth.db.models.UserDetailsEntity
import com.t3ddyss.clother.data.auth.db.models.UserEntity
import com.t3ddyss.clother.data.auth.db.models.UserWithDetailsEntity
import com.t3ddyss.clother.data.auth.remote.models.UserAuthDataDto
import com.t3ddyss.clother.data.auth.remote.models.UserDetailsDto
import com.t3ddyss.clother.data.auth.remote.models.UserDto
import com.t3ddyss.clother.data.chat.db.models.ChatEntity
import com.t3ddyss.clother.data.chat.db.models.ChatWithLastMessageEntity
import com.t3ddyss.clother.data.chat.db.models.MessageEntity
import com.t3ddyss.clother.data.chat.remote.models.ChatDto
import com.t3ddyss.clother.data.chat.remote.models.MessageDto
import com.t3ddyss.clother.data.common.common.remote.models.ResponseDto
import com.t3ddyss.clother.data.offers.db.models.CategoryEntity
import com.t3ddyss.clother.data.offers.db.models.OfferEntity
import com.t3ddyss.clother.data.offers.db.models.OfferWithUserEntity
import com.t3ddyss.clother.data.offers.remote.models.OfferDto
import com.t3ddyss.clother.domain.auth.models.AuthData
import com.t3ddyss.clother.domain.auth.models.User
import com.t3ddyss.clother.domain.auth.models.UserAuthData
import com.t3ddyss.clother.domain.auth.models.UserDetails
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.domain.common.common.models.Response
import com.t3ddyss.clother.domain.offers.models.Category
import com.t3ddyss.clother.domain.offers.models.Offer
import com.t3ddyss.navigation.presentation.models.CategoryArg
import com.t3ddyss.navigation.presentation.models.UserArg

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
            interlocutorId = this.interlocutor.id
        )
    }

    fun ChatDto.toDomain(isLastMessageIncoming: Boolean): Chat {
        return Chat(
            id = this.id,
            interlocutor = this.interlocutor.toDomain(),
            lastMessage = this.lastMessage.toDomain(isIncoming = isLastMessageIncoming)
        )
    }

    fun Chat.toEntity(): ChatEntity {
        return ChatEntity(
            serverId = this.id,
            interlocutorId = this.interlocutor.id
        )
    }

    fun ChatWithLastMessageEntity.toDomain(): Chat {
        val isIncoming = this.interlocutor.id == this.message.userId
        return Chat(
            id = this.chat.serverId ?: this.chat.localId,
            interlocutor = this.interlocutor.toDomain(),
            lastMessage = this.message.toDomain(isIncoming)
        )
    }

    fun Message.toEntity(): MessageEntity {
        return MessageEntity(
            serverId = this.serverId,
            serverChatId = this.serverChatId,
            userId = this.userId,
            createdAt = this.createdAt,
            body = this.body,
            image = this.image
        )
    }

    fun MessageDto.toEntity(): MessageEntity {
        return MessageEntity(
            serverId = this.id,
            serverChatId = this.chatId,
            userId = this.userId,
            createdAt = this.createdAt,
            body = this.body,
            image = this.images.firstOrNull()
        )
    }

    fun MessageDto.toDomain(isIncoming: Boolean): Message {
        return Message(
            localId = 0,
            serverId = this.id,
            serverChatId = this.chatId,
            userId = this.userId,
            userName = this.userName,
            createdAt = this.createdAt,
            status = MessageStatus.DELIVERED,
            body = this.body,
            image = this.images.firstOrNull(),
            isIncoming = isIncoming
        )
    }

    fun MessageEntity.toDomain(isIncoming: Boolean): Message {
        return Message(
            localId = this.localId,
            serverId = this.serverId,
            serverChatId = this.serverChatId,
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
            images = emptyList(),
            chatId = 0,
            userName = ""
        )
    }

    fun OfferDto.toDomain(): Offer {
        return Offer(
            id = this.id,
            user = this.user.toDomain(),
            createdAt = this.createdAt,
            title = this.title,
            description = this.description.orEmpty(),
            category = this.category,
            images = this.images,
            size = this.size.orEmpty(),
            location =this.location.orEmpty()
        )
    }

    fun OfferDto.toEntity(listKey: String): OfferEntity {
        return OfferEntity(
            id = this.id,
            listKey = listKey,
            categoryId = this.categoryId,
            userId = this.user.id,
            createdAt = this.createdAt,
            title = this.title,
            description = this.description.orEmpty(),
            category = this.category,
            images = this.images,
            size = this.size.orEmpty(),
            location = this.location.orEmpty()
        )
    }

    fun OfferWithUserEntity.toDomain(): Offer {
        return Offer(
            id = this.offer.id,
            user = user.toDomain(),
            createdAt = this.offer.createdAt,
            title = this.offer.title,
            description = this.offer.description.orEmpty(),
            category = this.offer.category,
            images = this.offer.images,
            size = this.offer.size.orEmpty(),
            location = this.offer.location.orEmpty()
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
            image = this.image
        )
    }

    fun UserDetails.toEntity(userId: Int): UserDetailsEntity {
        return UserDetailsEntity(
            userId = userId,
            email = this.email,
            createdAt = this.createdAt,
            status = this.status
        )
    }

    fun UserDto.toDomain(): User {
        return User(
            id = this.id,
            name = this.name,
            image = this.image.orEmpty(),
            details = this.details?.toDomain()
        )
    }

    fun UserDetailsDto.toDomain(): UserDetails {
        return UserDetails(
            email = this.email,
            createdAt = this.createdAt,
            status = this.status.orEmpty()
        )
    }

    fun UserDto.toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            name = this.name,
            image = this.image
        )
    }

    fun UserDetailsDto.toEntity(userId: Int): UserDetailsEntity {
        return UserDetailsEntity(
            userId = userId,
            email = this.email,
            createdAt = this.createdAt,
            status = this.status.orEmpty()
        )
    }

    fun UserEntity.toDomain(): User {
        return User(
            id = this.id,
            name = this.name,
            image = this.image.orEmpty()
        )
    }

    fun UserDetailsEntity.toDomain(): UserDetails {
        return UserDetails(
            email = this.email,
            createdAt = this.createdAt,
            status = this.status
        )
    }

    fun UserWithDetailsEntity.toDomain(): User {
        return User(
            id = this.user.id,
            name = this.user.name,
            image = this.user.image.orEmpty(),
            details = this.details.toDomain()
        )
    }

    fun UserAuthDataDto.toDomain(): UserAuthData {
        return UserAuthData(
            user = this.user.toDomain(),
            accessToken = this.accessToken,
            refreshToken = this.refreshToken
        )
    }

    fun UserAuthData.toAuthData(): AuthData {
        return AuthData(
            userId = this.user.id,
            accessToken = this.accessToken,
            refreshToken = this.refreshToken
        )
    }

    fun Category.toArg(): CategoryArg {
        return CategoryArg(
            id = this.id,
            title = this.title,
            isLastLevel = this.isLastLevel
        )
    }

    fun User.toArg(): UserArg {
        return UserArg(
            id = this.id,
            name = this.name
        )
    }
}
