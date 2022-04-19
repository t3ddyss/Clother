package com.t3ddyss.clother.data.chat

import com.google.gson.Gson
import com.t3ddyss.clother.data.chat.remote.models.ChatDto
import com.t3ddyss.clother.data.chat.remote.models.MessageDto
import com.t3ddyss.clother.data.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.Storage
import com.t3ddyss.clother.di.common.NetworkModule
import com.t3ddyss.clother.domain.chat.ChatListenerRepository
import com.t3ddyss.clother.domain.chat.models.Event
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatListenerRepositoryImpl @Inject constructor(
    @NetworkModule.BaseUrl
    private val baseUrl: String,
    private val storage: Storage,
    private val gson: Gson
) : ChatListenerRepository {

    private var socket: Socket? = null

    override fun observeEvents() = callbackFlow {
        initializeSocket()

        val onConnectListener = Emitter.Listener {
            trySend(Event.Connect)
        }

        val onDisconnectListener = Emitter.Listener {
            trySend(Event.Disconnect)
        }

        val onNewMessageListener = Emitter.Listener {
            val message = gson.fromJson(it[0] as? String, MessageDto::class.java)
            trySend(Event.NewMessage(message.toDomain(isIncoming = true)))
        }

        val onNewChatListener = Emitter.Listener {
            val chat = gson.fromJson(it[0] as? String, ChatDto::class.java)
            trySend(Event.NewChat(chat.toDomain(isLastMessageIncoming = true)))
        }

        socket?.on(EventKey.CONNECT.key, onConnectListener)
        socket?.on(EventKey.DISCONNECT.key, onDisconnectListener)
        socket?.on(EventKey.MESSAGE.key, onNewMessageListener)
        socket?.on(EventKey.CHAT.key, onNewChatListener)
        socket?.connect()

        awaitClose {
            socket?.off()
            socket?.disconnect()
        }
    }

    private fun initializeSocket() {
        val options = IO.Options.builder()
            .setTransports(arrayOf(WebSocket.NAME))
            .setExtraHeaders(
                mapOf(
                    "Authorization" to listOf(storage.accessToken),
                    "Content-type" to listOf("application/json")
                )
            )
            .build()
        socket = IO.socket(baseUrl, options)
    }

    private enum class EventKey(val key: String) {
        CONNECT(Socket.EVENT_CONNECT),
        DISCONNECT(Socket.EVENT_DISCONNECT),
        MESSAGE("message"),
        CHAT("chat")
    }
}