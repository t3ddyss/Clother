package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.BASE_URL_DEVICE
import com.t3ddyss.clother.utilities.DEBUG_TAG
import com.t3ddyss.clother.utilities.getBaseUrlForCurrentDevice
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@ExperimentalCoroutinesApi
class MessagesRepository @Inject constructor(
    private val prefs: SharedPreferences
) {
    private val options = IO.Options.builder()
            .setExtraHeaders(
                    mapOf("Authorization" to listOf(prefs.getString(ACCESS_TOKEN, "")),
                          "Content-type" to listOf("application/json")))
            .build()
    private val socket = IO.socket(getBaseUrlForCurrentDevice(), options)

    suspend fun getMessagesStream(): Flow<String> = callbackFlow {
        val onConnectListener = Emitter.Listener {
            offer(it.getOrNull(1) as? String ?: "Got non string object at it[1]")

            val fruit = JsonObject()
            fruit.addProperty("access_token", prefs.getString(ACCESS_TOKEN, ""))
            fruit.addProperty("fruit", "apple")
            socket.emit("join", fruit)
        }

        socket.connect()
        socket.on("connection", onConnectListener)

        awaitClose {
            socket.disconnect()
        }
    }.flowOn(Dispatchers.IO)

    fun disconnectFromServer() {
        socket.disconnect()
    }
}