package com.t3ddyss.clother.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.gson.Gson
import com.t3ddyss.clother.R
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.chat.MessageStatus
import com.t3ddyss.clother.utilities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
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


@Singleton
@ExperimentalCoroutinesApi
class LiveMessagesRepository @Inject constructor(
        private val service: ClotherChatService,
        private val prefs: SharedPreferences,
        private val db: AppDatabase,
        private val chatDao: ChatDao,
        private val messageDao: MessageDao,
        private val remoteKeyDao: RemoteKeyDao,
        private val gson: Gson,
        @ApplicationContext
        private val context: Context,
) {
    private val options = IO.Options.builder()
            .setTransports(arrayOf(WebSocket.NAME))
            .setExtraHeaders(
                    mapOf("Authorization" to listOf(prefs.getString(ACCESS_TOKEN, "")),
                          "Content-type" to listOf("application/json")))
            .build()
    private val socket = IO.socket(getBaseUrlForCurrentDevice(), options)
    private var notificationId = 0

    suspend fun getMessagesStream() = callbackFlow {
        val onConnectListener = Emitter.Listener {
        }

        val onNewMessageListener = Emitter.Listener {
            val message = gson.fromJson(it[0] as? String, Message::class.java)
            offer(message)

            launch {
                messageDao.insert(message)
            }

            showNotification(message)
        }

        socket.on("connection", onConnectListener)
        socket.on("message", onNewMessageListener)
        socket.connect()

        awaitClose {
            Log.d(DEBUG_TAG, "Going to disconnect in awaitClose()")
            socket.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMessage(to: Int, messageBody: String) {
        val message = Message(
                id = 0,
                chatId = chatDao.getChatByInterlocutorId(to)?.id ?: 0,
                userId = prefs.getInt(USER_ID, 0),
                status = MessageStatus.DELIVERING,
                createdAt = Calendar.getInstance().time,
                body = messageBody,
                image = null)

        message.id = messageDao.insert(message).toInt()
        socket.emit("send_message", gson.toJson(message), to)

        val sentMessage = withTimeoutOrNull(RESPONSE_TIMEOUT) {
            getSentMessage(message.id)
        }

        if (sentMessage != null) {
            messageDao.insert(sentMessage)
        }
        else {
            message.status = MessageStatus.FAILED
            messageDao.insert(message)
        }
    }

    private suspend fun getSentMessage(messageId: Int) =
            suspendCancellableCoroutine<Message> { cont ->
        val onMessageSentListener = Emitter.Listener {
            socket.off("message$messageId")
            cont.resume(gson.fromJson(it[0] as String, Message::class.java).also { message ->
                message.id = messageId
                message.status = MessageStatus.DELIVERED
            })
        }

        socket.on("message$messageId", onMessageSentListener)
    }

    fun disconnectFromServer() {
        Log.d(DEBUG_TAG, "Going to disconnect manually")

        socket.off()
        socket.disconnect()
    }

    private fun showNotification(message: Message) {
        val builder = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle(message.userName)
                .setContentText(message.body ?: context.getString(R.string.image))
                .setGroup(message.userId.toString())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId++, builder.build())
        }
    }

    companion object {
        const val RESPONSE_TIMEOUT = 5_000L
    }
}