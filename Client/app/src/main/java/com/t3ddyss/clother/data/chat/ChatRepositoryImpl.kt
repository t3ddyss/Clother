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
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Mappers.toDto
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.domain.chat.ChatRepository
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.LocalImage
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.util.nestedMap
import com.t3ddyss.clother.util.networkBoundResource
import com.t3ddyss.core.domain.models.User
import com.t3ddyss.core.util.log
import com.t3ddyss.core.util.rethrowIfCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    override suspend fun sendMessage(body: String?, image: LocalImage?, interlocutor: User) {
        val localChatId = chatDao.getChatByInterlocutorId(interlocutor.id)?.localId
            ?: return sendMessageToNewChat(body, image, interlocutor)

        val message = MessageEntity(
            localId = 0,
            localChatId = localChatId,
            userId = storage.userId,
            status = MessageStatus.DELIVERING,
            createdAt = Calendar.getInstance().time,
            body = body,
            image = image?.uri
        ).apply {
            this.localId = messageDao.insert(this).toInt()
        }

        try {
            val messageBody = gson
                .toJson(message.toDto())
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val imageFiles = image?.file?.let {
                listOf(
                    MultipartBody.Part.createFormData(
                        name = "file",
                        it.name,
                        it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                )
            }
            val messageDto = chatService.sendMessageAndGetIt(
                accessToken = storage.accessToken,
                interlocutorId = interlocutor.id,
                body = messageBody,
                images = imageFiles
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
                sendDeviceToken(token)
            } catch (ex: Exception) {
                ex.rethrowIfCancellationException()
                log("ChatRepositoryImpl.sendDeviceTokenIfNeeded() $ex")
            }
        }
    }

    override suspend fun sendDeviceToken(token: String) {
        try {
            authService.sendDeviceToken(storage.accessToken, token)
            storage.isDeviceTokenRetrieved = true
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ChatRepositoryImpl.sendDeviceToken() $ex")
        }
    }

    private suspend fun sendMessageToNewChat(body: String?, image: LocalImage?, interlocutor: User) {
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
                image = image?.uri
            ).apply {
                this.localId = messageDao.insert(this).toInt()
            }
        }

        try {
            val messageBody = gson
                .toJson(message.toDto())
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val imageFiles = image?.file?.let {
                listOf(
                    MultipartBody.Part.createFormData(
                        name = "file",
                        it.name,
                        it.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                )
            }
            val chatDto = chatService
                .sendMessageAndGetChat(
                    accessToken = storage.accessToken,
                    interlocutorId = interlocutor.id,
                    body = messageBody,
                    images = imageFiles
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