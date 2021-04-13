package com.t3ddyss.clother.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.t3ddyss.clother.R
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.utilities.*
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
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@ExperimentalCoroutinesApi
@ExperimentalPagingApi
class MessagesRepository @Inject constructor(
        private val service: ClotherChatService,
        private val prefs: SharedPreferences,
        private val db: AppDatabase,
        private val chatDao: ChatDao,
        private val messageDao: MessageDao,
        private val remoteKeyDao: RemoteKeyDao,
        @ApplicationContext
        private val context: Context
) {
    private val options = IO.Options.builder()
            .setTransports(arrayOf(WebSocket.NAME))
            .setExtraHeaders(
                    mapOf("Authorization" to listOf(prefs.getString(ACCESS_TOKEN, "")),
                          "Content-type" to listOf("application/json")))
            .build()
    private val socket = IO.socket(getBaseUrlForCurrentDevice(), options)
    private var notificationId = 0

    fun getMessages(interlocutorId: Int, remoteKey: String): Flow<PagingData<Message>> {
        return Pager(
                config = PagingConfig(
                        pageSize = CLOTHER_PAGE_SIZE_CHAT,
                        enablePlaceholders = false),
                remoteMediator = MessagesRemoteMediator(
                        service = service,
                        prefs = prefs,
                        db = db,
                        chatDao = chatDao,
                        messageDao = messageDao,
                        remoteKeyDao = remoteKeyDao,
                        remoteKeyList = remoteKey,
                        interlocutorId = interlocutorId),
                pagingSourceFactory = { messageDao.getMessagesByInterlocutorId(interlocutorId) }
        ).flow
    }

    suspend fun getMessagesStream(): Flow<String> = callbackFlow {
        val onConnectListener = Emitter.Listener {
            offer("Connected!")
        }

        val onNewMessageListener = Emitter.Listener {
            offer(it[0] as? String ?: "Error getting message")
            showNotification(it[0] as? String ?: "Error getting message")
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

    private fun showNotification(message: String) {
        val builder = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat)
                .setContentTitle("Received new message!")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId++, builder.build())
        }
    }
}