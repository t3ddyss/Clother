package com.t3ddyss.clother.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.google.gson.Gson
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.chat.MessageStatus
import com.t3ddyss.clother.models.user.User
import com.t3ddyss.clother.utilities.*
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// TODO find out how to refresh access token without creating "racing" condition with HTTP requests
@Singleton
@ExperimentalCoroutinesApi
class LiveMessagesRepository @Inject constructor(
        private val prefs: SharedPreferences,
        private val db: AppDatabase,
        private val chatDao: ChatDao,
        private val messageDao: MessageDao,
        private val notificationUtil: NotificationUtil,
        private val gson: Gson,
) {
    private var socket: Socket? = null
    private val userId by lazy {
        prefs.getInt(USER_ID, 0)
    }

    val isConnected get() = socket?.connected() ?: false
    var currentInterlocutorId: Int? = null
    var isChatsFragment = false

    private fun initializeSocket(): Socket {
        val options = IO.Options.builder()
                .setTransports(arrayOf(WebSocket.NAME))
                .setExtraHeaders(
                        mapOf("Authorization" to listOf(prefs.getString(ACCESS_TOKEN, "")),
                                "Content-type" to listOf("application/json")))
                .build()
        return IO.socket(getBaseUrlForCurrentDevice(), options)
    }

    suspend fun getMessagesStream() = callbackFlow {
        socket = initializeSocket()

        val onConnectListener = Emitter.Listener {
            offer(CONNECTED)
        }

        val onNewMessageListener = Emitter.Listener {
            val message = gson.fromJson(it[0] as? String, Message::class.java)

            launch {
                addNewMessage(message)
            }

            if (!isChatsFragment && currentInterlocutorId != message.userId) {
                notificationUtil.showNotification(message)
            }
        }

        val onNewChatListener = Emitter.Listener {
            val chat = gson.fromJson(it[0] as? String, Chat::class.java)

            launch {
                addNewChat(chat)
            }

            chat.lastMessage?.also { message ->
                notificationUtil.showNotification(message)
            }
        }

        socket?.on("connection", onConnectListener)
        socket?.on("message", onNewMessageListener)
        socket?.on("chat", onNewChatListener)
        socket?.connect()

        awaitClose {
            socket?.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMessage(to: User, messageBody: String) {
        val localChatId = chatDao.getChatByInterlocutorId(to.id)?.localId
                ?: return createChat(to, messageBody)

        val message = Message(
                localId = 0, // will be autogenerated after insertion
                localChatId = localChatId,
                userId = userId,
                status = MessageStatus.DELIVERING,
                createdAt = Calendar.getInstance().time,
                body = messageBody,
                image = null)

        message.localId = messageDao.insert(message).toInt()
        socket?.emit("send_message", gson.toJson(message), to.id)

        val sentMessage = withTimeoutOrNull(RESPONSE_TIMEOUT) {
            getSentMessage(message.localId, localChatId)
        }

        if (sentMessage != null) {
            sentMessage.status = MessageStatus.DELIVERED
            updateMessage(sentMessage)
        }
        else {
            message.status = MessageStatus.FAILED
            updateMessage(message)
        }
    }

    private suspend fun createChat(to: User, messageBody: String) {
        val chat = Chat(interlocutor = to)
        var message: Message? = null

        db.withTransaction {
            val localChatId = chatDao.insert(chat).toInt()
            chat.localId = localChatId

            message = Message(
                    localId = 0, // will be autogenerated after insertion
                    localChatId = localChatId,
                    userId = userId,
                    status = MessageStatus.DELIVERING,
                    createdAt = Calendar.getInstance().time,
                    body = messageBody,
                    image = null)

            message?.let {
                it.localId = messageDao.insert(it).toInt()
            }
        }


        if (message != null) {
            socket?.emit("send_message", gson.toJson(message), to.id)

            val createdChat = withTimeoutOrNull(RESPONSE_TIMEOUT) {
                getCreatedChat(message?.localId ?: return@withTimeoutOrNull null,
                        chat.localId)
            }

            if (createdChat != null) {
                message?.status = MessageStatus.DELIVERED
                addNewChat(chat = createdChat, temporaryChat = chat, temporaryMessage = message)
            }
            else {
                message?.status = MessageStatus.FAILED
                updateMessage(message ?: return)
            }
        }
    }

    private suspend fun getSentMessage(localMessageId: Int, localChatId: Int) =
            suspendCancellableCoroutine<Message> { cont ->
        val onMessageSentListener = Emitter.Listener {
            socket?.off("message$localMessageId")
            cont.resume(gson.fromJson(it[0] as String, Message::class.java).also { message ->
                message.localId = localMessageId
                message.localChatId = localChatId
            })
        }

        socket?.on("message$localMessageId", onMessageSentListener)
    }

    private suspend fun getCreatedChat(localMessageId: Int, localChatId: Int) =
            suspendCancellableCoroutine<Chat> { cont ->
        val onChatCreatedListener = Emitter.Listener {
            socket?.off("message$localMessageId")
            cont.resume(gson.fromJson(it[0] as String, Chat::class.java).also { chat ->
                chat.localId = localChatId
//                chat.lastMessage?.localId = localMessageId
//                chat.lastMessage?.localChatId = localChatId
            })
        }

        socket?.on("message$localMessageId", onChatCreatedListener)
    }

    private suspend fun updateMessage(message: Message) {
        messageDao.update(message)
    }

    private suspend fun addNewMessage(message: Message) {
        val chat = chatDao.getChatByInterlocutorId(message.userId)

        chat?.let {
            message.localChatId = chat.localId
            messageDao.insert(message)
        }

        // TODO handle situation when chat is not in cache
    }

    private suspend fun addNewChat(chat: Chat,
                                   temporaryChat: Chat? = null,
                                   temporaryMessage: Message? = null) {
        db.withTransaction {
            if (temporaryMessage != null) {
                messageDao.delete(temporaryMessage)
            }

            if (temporaryChat != null) {
                chatDao.delete(temporaryChat)
            }

            val message = chat.lastMessage!!

            chat.lastMessage = null
            message.localChatId = chatDao.insert(chat).toInt()
            message.localId = messageDao.insert(message).toInt()
            chat.lastMessage = message
            chatDao.insert(chat)
        }
    }

    fun disconnectFromServer() {
        socket?.off()
        socket?.disconnect()
    }

    companion object {
        const val RESPONSE_TIMEOUT = 5_000L
        const val CONNECTED = "connected"
    }
}