package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.BASE_URL_DEVICE
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.getBaseUrlForCurrentDevice
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_CONNECT_ERROR
import io.socket.emitter.Emitter
import io.socket.engineio.client.EngineIOException
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import java.util.*
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
@ExperimentalCoroutinesApi
class MessagesRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    private val options = IO.Options.builder()
            .setTransports(arrayOf(WebSocket.NAME))
            .setExtraHeaders(
                    mapOf("Authorization" to listOf(prefs.getString(ACCESS_TOKEN, "")),
                          "Content-type" to listOf("application/json")))
            .build()
    private val socket = IO.socket(getBaseUrlForCurrentDevice(), options)

    suspend fun getMessagesStream(): Flow<String> = callbackFlow {
        val onConnectListener = Emitter.Listener {
            offer("Connected!")
        }

        val onNewMessageListener = Emitter.Listener {
            offer(it[0] as? String ?: "Error getting message")
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

    fun sendMessage(to: Int = 137, message: String) {
        socket.emit("new_message", message, to)
    }

    fun disconnectFromServer() {
        Log.d(DEBUG_TAG, "Going to disconnect manually")
        socket.off()
        socket.disconnect()
    }
}