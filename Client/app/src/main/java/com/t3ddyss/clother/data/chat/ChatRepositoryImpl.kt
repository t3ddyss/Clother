package com.t3ddyss.clother.data.chat

import android.net.Uri
import androidx.room.withTransaction
import arrow.core.Either
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.t3ddyss.clother.data.auth.db.UserDao
import com.t3ddyss.clother.data.auth.remote.RemoteAuthService
import com.t3ddyss.clother.data.chat.db.ChatDao
import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.chat.db.models.ChatEntity
import com.t3ddyss.clother.data.chat.db.models.MessageEntity
import com.t3ddyss.clother.data.chat.remote.RemoteChatService
import com.t3ddyss.clother.data.chat.remote.models.ChatDto
import com.t3ddyss.clother.data.common.common.Mappers.toApiCallError
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.common.Mappers.toDto
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.domain.chat.ChatRepository
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.ChatsState
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.domain.offers.ImagesRepository
import com.t3ddyss.core.domain.models.ApiCallError
import com.t3ddyss.core.util.extensions.mapList
import com.t3ddyss.core.util.extensions.rethrowIfCancellationException
import com.t3ddyss.core.util.log
import kotlinx.coroutines.flow.*
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
    private val imagesRepository: ImagesRepository,
    private val authService: RemoteAuthService,
    private val chatService: RemoteChatService,
    private val db: AppDatabase,
    private val userDao: UserDao,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val messagesPagingLoader: MessagesPagingLoader,
    private val storage: Storage,
    private val gson: Gson
) : ChatRepository {

    override fun observeChatsFromDatabase(): Flow<ChatsState> = flow {
        val cache = chatDao.observeChats().first().map { it.toDomain() }
        emit(ChatsState.Cache(cache))

        chatService.getChats(storage.accessToken)
            .tap { chats ->
                storeChats(chats)
                emitAll(
                    chatDao.observeChats()
                        .mapList { it.toDomain() }
                        .map(ChatsState::Fetched)
                )
            }
            .tapLeft { error ->
                ChatsState.Error(cache, error.toApiCallError())
            }
    }

    override fun observeMessagesForChatFromDatabase(interlocutorId: Int): Flow<List<Message>> {
        return messageDao
            .observeMessagesByInterlocutorId(interlocutorId)
            .mapList { it.toDomain(it.userId == interlocutorId) }
    }

    override suspend fun fetchNextPortionOfMessagesForChat(interlocutorId: Int): LoadResult {
        return messagesPagingLoader.load(
            listKey = LIST_KEY_MESSAGES + interlocutorId,
            interlocutorId = interlocutorId
        )
    }

    override suspend fun sendMessage(body: String?, image: Uri?, interlocutorId: Int) {
        val localChatId = chatDao.getChatByInterlocutorId(interlocutorId)?.localId
            ?: return sendMessageToNewChat(body, image,  interlocutorId)

        val message = MessageEntity(
            localId = 0,
            localChatId = localChatId,
            userId = storage.userId,
            status = MessageStatus.DELIVERING,
            createdAt = Calendar.getInstance().time,
            body = body,
            image = image?.toString()
        ).apply {
            this.localId = messageDao.insert(this).toInt()
        }
        sendMessageImpl(message, image, interlocutorId)
    }

    override suspend fun retryToSendMessage(messageLocalId: Int) {
        val message = messageDao.getMessageByLocalId(messageLocalId)
        val chat = chatDao.getChatByLocalId(message.localChatId)
        val interlocutorId = chat?.interlocutorId?.let {
            userDao.getUserById(it).id
        }

        if (interlocutorId != null) {
            val image = message.image?.let {
                Uri.parse(it)
            }
            if (chat.serverId != null) {
                sendMessageImpl(message, image, interlocutorId)
            } else {
                sendMessageToNewChatImpl(chat, message, image, interlocutorId)
            }
        }
    }

    override suspend fun deleteMessage(messageLocalId: Int): Either<ApiCallError, Unit> {
        val messageServerId = messageDao.getMessageByLocalId(messageLocalId).serverId

        return if (messageServerId != null) {
            chatService.deleteMessage(storage.accessToken, messageServerId).mapLeft { it.toApiCallError() }
        } else {
            Either.Right(Unit)
        }.tap {
            messageDao.deleteByLocalId(messageLocalId)
        }
    }

    override suspend fun addNewMessage(message: Message) {
        val chat = message.serverChatId?.let {
            chatDao.getChatByServerId(it)
        }

        // TODO handle scenario when chat is not yet in cache
        chat?.let {
            messageDao.insert(message.toEntity().also {
                it.localChatId = chat.localId
            })
        }
    }

    override suspend fun addNewChat(chat: Chat) {
        db.withTransaction {
            userDao.insert(chat.interlocutor.toEntity())
            val message = chat.lastMessage.toEntity()
            message.localChatId = chatDao.insert(chat.toEntity()).toInt()
            messageDao.insert(message)
        }
    }

    override suspend fun removeMessage(messageId: Int) {
        messageDao.deleteByServerId(messageId)
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

    private suspend fun sendMessageImpl(
        message: MessageEntity,
        image: Uri?,
        interlocutorId: Int
    ) {
        if (message.status != MessageStatus.DELIVERING) {
            message.status = MessageStatus.DELIVERING
            updateMessage(message)
        }

        try {
            val messageBody = gson
                .toJson(message.toDto())
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val imageFiles = image?.let {
                val file = imagesRepository.getCompressedImage(it)
                listOf(
                    MultipartBody.Part.createFormData(
                        name = "file",
                        file.name,
                        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                )
            }
            val messageDto = chatService.sendMessageAndGetIt(
                accessToken = storage.accessToken,
                interlocutorId = interlocutorId,
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

    private suspend fun sendMessageToNewChat(body: String?, image: Uri?, interlocutorId: Int) {
        val chat = ChatEntity(
            interlocutorId = interlocutorId
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
                image = image?.toString()
            ).apply {
                this.localId = messageDao.insert(this).toInt()
            }
        }

        sendMessageToNewChatImpl(chat, message, image, interlocutorId)
    }

    private suspend fun sendMessageToNewChatImpl(
        chat: ChatEntity,
        message: MessageEntity,
        image: Uri?,
        interlocutorId: Int
    ) {
        if (message.status != MessageStatus.DELIVERING) {
            message.status = MessageStatus.DELIVERING
            updateMessage(message)
        }

        try {
            val messageBody = gson
                .toJson(message.toDto())
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            val imageFiles = image?.let {
                val file = imagesRepository.getCompressedImage(it)
                listOf(
                    MultipartBody.Part.createFormData(
                        name = "file",
                        file.name,
                        file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                )
            }
            val chatDto = chatService
                .sendMessageAndGetChat(
                    accessToken = storage.accessToken,
                    interlocutorId = interlocutorId,
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

    private suspend fun storeChats(chats: List<ChatDto>) {
        db.withTransaction {
            messageDao.deleteLocalMessages()
            chatDao.deleteLocalChats()

            userDao.insertAll(chats.map { it.interlocutor.toEntity() })
            chatDao.insertAll(chats.map { it.toEntity() })
                .zip(chats)
                .map {
                    it.second.lastMessage.toEntity().apply {
                        localChatId = it.first.toInt()
                    }
                }.let {
                    messageDao.insertAll(it)
                }
        }
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