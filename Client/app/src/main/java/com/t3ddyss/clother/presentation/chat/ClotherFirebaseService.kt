package com.t3ddyss.clother.presentation.chat

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.t3ddyss.clother.data.chat.remote.models.ChatDto
import com.t3ddyss.clother.data.chat.remote.models.MessageDto
import com.t3ddyss.clother.data.common.common.Mappers.toDomain
import com.t3ddyss.clother.domain.chat.ChatInteractor
import com.t3ddyss.clother.domain.chat.models.CloudEvent
import com.t3ddyss.core.util.log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ClotherFirebaseService : FirebaseMessagingService() {
    @Inject
    lateinit var chatInteractor: ChatInteractor
    @Inject
    lateinit var gson: Gson

    override fun onNewToken(token: String) {
        chatInteractor.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val chatJson = remoteMessage.data[CHAT_KEY]
        val messageJson = remoteMessage.data[MESSAGE_KEY]

        try {
            when {
                messageJson != null -> {
                    val message = gson.fromJson(messageJson, MessageDto::class.java)
                        .toDomain(true)
                    chatInteractor.onNewCloudEvent(CloudEvent.NewMessage(message))
                }
                chatJson != null -> {
                    val chat = gson.fromJson(chatJson, ChatDto::class.java)
                        .toDomain(true)
                    chatInteractor.onNewCloudEvent(CloudEvent.NewChat(chat))
                }
                else -> {
                    log("ClotherFirebaseService.onMessageReceived(): payload is null")
                }
            }
        } catch (ex: Exception) {
            log("ClotherFirebaseService.onMessageReceived() $ex")
        }
    }

    private companion object {
        const val MESSAGE_KEY = "message"
        const val CHAT_KEY = "chat"
    }
}