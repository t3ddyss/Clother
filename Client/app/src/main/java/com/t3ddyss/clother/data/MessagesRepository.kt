package com.t3ddyss.clother.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.t3ddyss.clother.R
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
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
@ExperimentalCoroutinesApi
class MessagesRepository @Inject constructor(
    private val prefs: SharedPreferences,
    @ApplicationContext private val context: Context
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