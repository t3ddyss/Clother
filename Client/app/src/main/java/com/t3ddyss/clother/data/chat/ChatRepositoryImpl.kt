package com.t3ddyss.clother.data.chat

import androidx.room.withTransaction
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.chat.db.ChatDao
import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.chat.db.models.ChatEntity
import com.t3ddyss.clother.data.chat.db.models.MessageEntity
import com.t3ddyss.clother.data.chat.remote.RemoteChatService
import com.t3ddyss.clother.data.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.Mappers.toDto
import com.t3ddyss.clother.data.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.Storage
import com.t3ddyss.clother.data.common.db.AppDatabase
import com.t3ddyss.clother.domain.chat.ChatRepository
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.domain.common.models.LoadResult
import com.t3ddyss.clother.util.nestedMap
import com.t3ddyss.clother.util.networkBoundResource
import com.t3ddyss.core.domain.models.User
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.rethrowIfCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ChatRepositoryImpl @Inject constructor(
    private val authService: RemoteAuthService,
    private val chatService: RemoteChatService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val messagesPagingLoader: MessagesPagingLoader,
    private val storage: Storage,
    private val gson: Gson
) : ChatRepository {

    override fun observeChatsFromDatabase() = networkBoundResource(
        query = { chatDao.observeChats().nestedMap { it.toDomain() } },
        fetch = { chatService.getChats(storage.accessToken) },
        saveFetchResult = { chats ->
            db.withTransaction {
                chatDao.deleteUncreatedChats(
                    chats.map { it.id.toLong() }.toTypedArray()
                )
                messageDao.deleteUnsentMessages()

                val ids = chatDao.insertAll(
                    chats.map { it.toEntity() }
                )

                messageDao.insertAll(
                    ids
                        .zip(chats)
                        .map { chatWithId ->
                            chatWithId.second.lastMessage.toEntity().also {
                                it.localChatId = chatWithId.first.toInt()
                            }
                        }
                )
            }
        }
    )

    override fun observeMessagesForChatFromDatabase(interlocutor: User): Flow<List<Message>> {
        return messageDao
            .observeMessagesByInterlocutorId(interlocutor.id)
            .map { messages ->
                messages.map {
                    it.toDomain(it.userId == interlocutor.id)
                }
            }
    }

    override suspend fun fetchNextPortionOfMessagesForChat(interlocutor: User): LoadResult {
        return messagesPagingLoader.load(
            listKey = LIST_KEY_MESSAGES + interlocutor.id,
            interlocutorId = interlocutor.id
        )
    }

    override suspend fun sendMessage(body: String, image: File?, interlocutor: User) {
        val localChatId = chatDao.getChatByInterlocutorId(interlocutor.id)?.localId
            ?: return sendMessageToNewChat(body, image, interlocutor)

        val message = MessageEntity(
            localId = 0,
            localChatId = localChatId,
            userId = storage.userId,
            status = MessageStatus.DELIVERING,
            createdAt = Calendar.getInstance().time,
            body = body,
            image = null // TODO work with images
        ).apply {
            this.localId = messageDao.insert(this).toInt()
        }

        try {
            val messageJson = gson.toJson(message.toDto())
            val messageDto = chatService
                .sendMessageAndGetIt(
                    accessToken = storage.accessToken,
                    interlocutorId = interlocutor.id ,
                    messageJson = messageJson
                )

            message.status = MessageStatus.DELIVERED
            message.serverId = messageDto.id
            message.serverChatId = messageDto.chatId
            message.createdAt = messageDto.createdAt
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ChatRepositoryImpl.sendMessage() $ex")

            message.status = MessageStatus.FAILED
        } finally {
            updateMessage(message)
        }
    }

    override suspend fun addNewMessage(message: Message) {
        val chat = chatDao.getChatByInterlocutorId(message.userId)

        // TODO handle scenario when chat is not yet in cache
        chat?.let {
            messageDao.insert(message.toEntity().also {
                it.localChatId = chat.localId
            })
        }
    }

    override suspend fun addNewChat(chat: Chat) {
        db.withTransaction {
            val message = chat.lastMessage.toEntity()
            message.localChatId = chatDao.insert(chat.toEntity()).toInt()
            messageDao.insert(message)
        }
    }

    override suspend fun sendDeviceTokenIfNeeded() {
        if (!storage.isDeviceTokenRetrieved) {
            try {
                val token = requestCloudMessagingToken()
                authService.sendDeviceToken(storage.accessToken, token)
                storage.isDeviceTokenRetrieved = true
            } catch (ex: Exception) {
                ex.rethrowIfCancellationException()
                log("ChatRepositoryImpl.sendDeviceTokenIfNeeded() $ex")
            }
        }
    }

    private suspend fun sendMessageToNewChat(body: String, image: File?, interlocutor: User) {
        val chat = ChatEntity(
            interlocutor = interlocutor.toEntity()
        )

        val message = db.withTransaction {
            val localChatId = chatDao.insert(chat).toInt()
            chat.localId = localChatId

            MessageEntity(
                localId = 0,
                localChatId = localChatId,
                userId = storage.userId,
                status = MessageStatus.DELIVERING,
                createdAt = Calendar.getInstance().time,
                body = body,
                image = null // TODO work with images
            ).apply {
                this.localId = messageDao.insert(this).toInt()
            }
        }

        try {
            val messageJson = gson.toJson(message.toDto())
            val chatDto = chatService
                .sendMessageAndGetChat(
                    accessToken = storage.accessToken,
                    interlocutorId = interlocutor.id,
                    messageJson = messageJson
                )
            val messageDto = chatDto.lastMessage

            chat.serverId = chatDto.id
            message.status = MessageStatus.DELIVERED
            message.serverId = messageDto.id
            message.serverChatId = messageDto.chatId
            message.createdAt = messageDto.createdAt

            db.withTransaction {
                updateChat(chat)
                updateMessage(message)
            }
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ChatRepositoryImpl.sendMessage() $ex")

            message.status = MessageStatus.FAILED
            updateMessage(message)
        }
    }

    private suspend fun updateMessage(message: MessageEntity) {
        messageDao.update(message)
    }

    private suspend fun updateChat(chat: ChatEntity) {
        chatDao.update(chat)
    }

    private suspend fun requestCloudMessagingToken() = suspendCoroutine<String> { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                cont.resume(task.result)
            } else {
                cont.resumeWithException(task.exception!!)
            }
        }
    }

    private companion object {
        const val LIST_KEY_MESSAGES = "messages"
    }
}