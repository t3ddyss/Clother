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
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.MESSAGES_CHANNEL_ID
import com.t3ddyss.clother.utilities.getBaseUrlForCurrentDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import io.socket.client.IO
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@ExperimentalCoroutinesApi
class LiveMessagesRepository @Inject constructor(
        private val service: ClotherChatService,
        private val prefs: SharedPreferences,
        private val db: AppDatabase,
        private val chatDao: ChatDao,
        private val messageDao: MessageDao,
        private val remoteKeyDao: RemoteKeyDao,
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

    suspend fun getMessagesStream(): Flow<String> = callbackFlow {
        val onConnectListener = Emitter.Listener {
            offer("Connected!")
        }

        val onNewMessageListener = Emitter.Listener {
            val gson = Gson()
            val message = gson.fromJson(it[0] as? String, Message::class.java)

            offer(it[0] as? String ?: "Error getting message")
            showNotification(message)

            launch {
                messageDao.insertAll(listOf(message))
            }
        }

        socket.on("connection", onConnectListener)
        socket.on("message", onNewMessageListener)

        Log.d(DEBUG_TAG, "Going to connect")
        socket.connect()

        awaitClose {
            Log.d(DEBUG_TAG, "Going to disconnect in awaitClose()")
            socket.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    fun sendMessage(to: Int = 1, message: String) {
        socket.emit("send_message", message, to)
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
                .setContentText(message.body ?: "Image")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId++, builder.build())
        }
    }
}