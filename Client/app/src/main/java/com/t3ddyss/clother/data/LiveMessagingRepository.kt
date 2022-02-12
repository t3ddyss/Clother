package com.t3ddyss.clother.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.di.NetworkModule
import com.t3ddyss.clother.models.domain.AuthState
import com.t3ddyss.clother.models.domain.MessageStatus
import com.t3ddyss.clother.models.dto.ChatDto
import com.t3ddyss.clother.models.dto.MessageDto
import com.t3ddyss.clother.models.entity.ChatEntity
import com.t3ddyss.clother.models.entity.MessageEntity
import com.t3ddyss.clother.models.mappers.*
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.CURRENT_USER_ID
import com.t3ddyss.clother.utilities.IS_DEVICE_TOKEN_RETRIEVED
import com.t3ddyss.clother.utilities.NotificationHelper
import com.t3ddyss.core.domain.models.User
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// TODO find out how to refresh access token without creating racing condition with HTTP requests
@Singleton
class LiveMessagingRepository @Inject constructor(
    @NetworkModule.BaseUrl
    private val baseUrl: String,
    private val authService: ClotherAuthService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val authStateObserver: AuthStateObserver,
    private val notificationHelper: NotificationHelper,
    private val prefs: SharedPreferences,
    private val gson: Gson,
) {
    private var job: Job? = null
    private var socket: Socket? = null
    private val currentUserId get() = prefs.getInt(CURRENT_USER_ID, 0)
    private val currentAccessToken get() = prefs.getString(ACCESS_TOKEN, "")

    fun initialize() {
        MainScope().launch {
            authStateObserver.authState.collect {
                when (it) {
                    is AuthState.None -> {
                        job?.cancel()
                    }
                    is AuthState.Refreshing -> {
                        // TODO Pause observing?
                    }
                    is AuthState.Authenticated -> {
                        sendDeviceTokenToServerIfNeeded()

                        job?.cancel()
                        job = MainScope().launch {
                            observeMessages().collect()
                        }
                    }
                }
            }
        }
    }

    private suspend fun observeMessages() = callbackFlow {
        socket = initializeSocket()

        val onConnectListener = Emitter.Listener {
            trySend(CONNECTED)
        }

        val onNewMessageListener = Emitter.Listener {
            val message = gson.fromJson(it[0] as? String, MessageDto::class.java)
            launch {
                addNewMessage(message)
            }

            notificationHelper.showNotificationIfShould(
                mapMessageDtoToDomain(message, true)
            )
        }

        val onNewChatListener = Emitter.Listener {
            val chat = gson.fromJson(it[0] as? String, ChatDto::class.java)
            launch {
                addNewChat(chat)
            }

            notificationHelper.showNotificationIfShould(
                mapMessageDtoToDomain(chat.lastMessage, true)
            )
        }

        socket?.on("connection", onConnectListener)
        socket?.on("message", onNewMessageListener)
        socket?.on("chat", onNewChatListener)
        socket?.connect()

        awaitClose {
            disconnect()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMessage(to: User, messageBody: String) {
        val localChatId = chatDao.getChatByInterlocutorId(to.id)?.localId
            ?: return sendMessageToNewChat(to, messageBody)

        val message = MessageEntity(
            localId = 0,
            localChatId = localChatId,
            userId = currentUserId,
            status = MessageStatus.DELIVERING,
            createdAt = Calendar.getInstance().time,
            body = messageBody,
            image = null
        )

        message.localId = messageDao.insert(message).toInt()
        socket?.emit("send_message", gson.toJson(mapMessageEntityToDto(message)), to.id)

        val newMessageResult = withTimeoutOrNull(RESPONSE_TIMEOUT) {
            getNewMessageResult(message.localId)
        }

        if (newMessageResult != null) {
            message.status = MessageStatus.DELIVERED
            message.serverId = newMessageResult.id
            message.serverChatId = newMessageResult.chatId
            message.createdAt = newMessageResult.createdAt
            updateMessage(message)
        } else {
            message.status = MessageStatus.FAILED
            updateMessage(message)
        }
    }

    private suspend fun sendMessageToNewChat(to: User, messageBody: String) {
        val chat = ChatEntity(
            interlocutor = mapUserDomainToEntity(to)
        )

        val message = db.withTransaction {
            val localChatId = chatDao.insert(chat).toInt()
            chat.localId = localChatId

            MessageEntity(
                localId = 0,
                localChatId = localChatId,
                userId = currentUserId,
                status = MessageStatus.DELIVERING,
                createdAt = Calendar.getInstance().time,
                body = messageBody,
                image = null
            )
                .apply {
                    this.localId = messageDao.insert(this).toInt()
                }
        }


        socket?.emit("send_message", gson.toJson(mapMessageEntityToDto(message)), to.id)

        val newChatResult = withTimeoutOrNull(RESPONSE_TIMEOUT) {
            getNewChatResult(message.localId)
        }

        if (newChatResult != null) {
            chat.serverId = newChatResult.id

            message.status = MessageStatus.DELIVERED
            message.serverId = newChatResult.lastMessage.id
            message.serverChatId = newChatResult.lastMessage.chatId
            message.createdAt = newChatResult.lastMessage.createdAt

            db.withTransaction {
                updateChat(chat)
                updateMessage(message)
            }
        } else {
            message.status = MessageStatus.FAILED
            updateMessage(message)
        }
    }

    private suspend fun getNewMessageResult(localMessageId: Int) =
        suspendCancellableCoroutine<MessageDto> { cont ->
            val onNewMessageResultListener = Emitter.Listener {
                socket?.off("message$localMessageId")
                cont.resume(gson.fromJson(it[0] as String, MessageDto::class.java))
            }

            socket?.on("message$localMessageId", onNewMessageResultListener)
        }

    private suspend fun getNewChatResult(localMessageId: Int) =
        suspendCancellableCoroutine<ChatDto> { cont ->
            val onNewChatResultListener = Emitter.Listener {
                socket?.off("message$localMessageId")
                cont.resume(gson.fromJson(it[0] as String, ChatDto::class.java))
            }

            socket?.on("message$localMessageId", onNewChatResultListener)
        }

    private suspend fun updateMessage(message: MessageEntity) {
        messageDao.update(message)
    }

    private suspend fun updateChat(chat: ChatEntity) {
        chatDao.update(chat)
    }

    private suspend fun addNewMessage(message: MessageDto) {
        val chat = chatDao.getChatByInterlocutorId(message.userId)

        // TODO handle situation when chat is not yet in cache
        chat?.let {
            messageDao.insert(mapMessageDtoToEntity(message).also {
                it.localChatId = chat.localId
            })
        }
    }

    private suspend fun addNewChat(chat: ChatDto) {
        db.withTransaction {
            val message = mapMessageDtoToEntity(chat.lastMessage)
            message.localChatId = chatDao.insert(mapChatDtoToEntity(chat)).toInt()
            messageDao.insert(message)
        }
    }

    private suspend fun sendDeviceTokenToServerIfNeeded() {
        if (prefs.getBoolean(IS_DEVICE_TOKEN_RETRIEVED, false)) return

        val token = setupCloudMessaging()
        sendDeviceTokenToServer(token)
    }

    private suspend fun sendDeviceTokenToServer(token: String?) = runCatching {
        token?.let {
            authService.sendDeviceToken(prefs.getString(ACCESS_TOKEN, null), token)
            prefs.edit().putBoolean(IS_DEVICE_TOKEN_RETRIEVED, true).apply()
        }
    }

    private suspend fun setupCloudMessaging() = suspendCancellableCoroutine<String?> { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                cont.resume(null)
            }

            cont.resume(task.result)
        }
    }

    fun setCurrentInterlocutorId(interlocutorId: Int) {
        notificationHelper.currentInterlocutorId = interlocutorId
    }

    private fun initializeSocket(): Socket {
        val options = IO.Options.builder()
            .setTransports(arrayOf(WebSocket.NAME))
            .setExtraHeaders(
                mapOf(
                    "Authorization" to listOf(currentAccessToken),
                    "Content-type" to listOf("application/json")
                )
            )
            .build()
        return IO.socket(baseUrl, options)
    }

    private fun disconnect() {
        socket?.off()
        socket?.disconnect()
    }

    companion object {
        const val RESPONSE_TIMEOUT = 5_000L
        const val CONNECTED = "connected"
    }
}