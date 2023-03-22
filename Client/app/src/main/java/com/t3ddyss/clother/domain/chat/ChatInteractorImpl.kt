package com.t3ddyss.clother.domain.chat

import android.net.Uri
import arrow.core.Either
import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.domain.chat.models.*
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.util.DispatchersProvider
import com.t3ddyss.core.domain.models.ApiCallError
import com.t3ddyss.core.util.extensions.rethrowIfCancellationException
import com.t3ddyss.core.util.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatInteractorImpl @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val notificationInteractor: NotificationInteractor,
    private val chatRepository: ChatRepository,
    private val chatListenerRepository: ChatListenerRepository,
    private val scope: CoroutineScope,
    private val dispatchers: DispatchersProvider
) : ChatInteractor {

    private var eventsJob: Job? = null

    override fun initialize() {
        scope.launch(dispatchers.io) {
            authInteractor.authStateFlow.collect {
                when (it) {
                    is AuthState.None -> onAuthFailure()
                    is AuthState.Authenticated -> onAuthSuccess()
                }
            }
        }
    }

    override fun observeChats(): Flow<ChatsState> {
        return chatRepository.observeChatsFromDatabase()
    }

    override fun observeMessagesForChat(interlocutorId: Int): Flow<List<Message>> {
        return chatRepository.observeMessagesForChatFromDatabase(interlocutorId)
    }

    override suspend fun fetchNextPortionOfMessagesForChat(interlocutorId: Int): LoadResult {
        return chatRepository.fetchNextPortionOfMessagesForChat(interlocutorId)
    }

    override suspend fun sendMessage(body: String?, image: Uri?, interlocutorId: Int) {
        if (!body.isNullOrBlank() || image != null) {
            chatRepository.sendMessage(body, image, interlocutorId)
        }
    }

    override suspend fun retryToSendMessage(messageLocalId: Int) {
        chatRepository.retryToSendMessage(messageLocalId)
    }

    override suspend fun deleteMessage(messageLocalId: Int): Either<ApiCallError, Unit> {
        return chatRepository.deleteMessage(messageLocalId)
    }

    override fun onNewToken(token: String) {
        scope.launch {
            chatRepository.sendDeviceToken(token)
        }
    }

    override fun onNewCloudEvent(cloudEvent: CloudEvent) {
        scope.launch(dispatchers.io) {
            when (cloudEvent) {
                is CloudEvent.NewMessage -> onNewMessage(cloudEvent.message)
                is CloudEvent.NewChat -> onNewChat(cloudEvent.chat)
            }
        }
    }

    private suspend fun onAuthSuccess() {
        eventsJob = scope.launch(dispatchers.io) {
            chatListenerRepository
                .observeEvents()
                .cancellable()
                .catch { log("ChatInteractorImpl.startObservingEvents() error: $it") }
                .onEach { log("ChatInteractorImpl.startObservingEvents() onEach: $it") }
                .flowOn(dispatchers.io)
                .collect {
                    when (it) {
                        is Event.Connect -> onConnect()
                        is Event.Disconnect -> onDisconnect()
                        is Event.NewMessage -> onNewMessage(it.message)
                        is Event.NewChat -> onNewChat(it.chat)
                        is Event.DeleteMessage -> onDeleteMessage(it.messageId)
                    }
                }
        }
        chatRepository.sendDeviceTokenIfNeeded()
    }

    private fun onAuthFailure() {
        eventsJob?.cancel()
    }

    private fun onConnect() {
        log("ChatInteractorImpl.onConnect()")
    }

    private fun onDisconnect() {
        log("ChatInteractorImpl.onDisconnect()")
    }

    private suspend fun onNewMessage(message: Message) {
        try {
            chatRepository.addNewMessage(message)
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ChatInteractorImpl.onNewMessage(message = ${message.body}): $ex")
        }

        notificationInteractor.showMessageNotificationIfNeeded(message)
    }

    private suspend fun onNewChat(chat: Chat) {
        try {
            chatRepository.addNewChat(chat)
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ChatInteractorImpl.onNewChat(chatId = ${chat.id}): $ex")
        }

        notificationInteractor.showMessageNotificationIfNeeded(chat.lastMessage)
    }

    private suspend fun onDeleteMessage(messageId: Int) {
        try {
            chatRepository.removeMessage(messageId)
        } catch (ex: Exception) {
            ex.rethrowIfCancellationException()
            log("ChatInteractorImpl.onDeleteMessage(messageId = $messageId): $ex")
        }
    }
}