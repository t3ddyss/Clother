package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.auth.models.AuthState
import com.t3ddyss.clother.domain.chat.models.*
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.domain.offers.ImagesInteractor
import com.t3ddyss.clother.util.DispatchersProvider
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.User
import com.t3ddyss.core.util.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class ChatInteractorImpl @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val notificationInteractor: NotificationInteractor,
    private val imagesInteractor: ImagesInteractor,
    private val chatRepository: ChatRepository,
    private val chatListenerRepository: ChatListenerRepository,
    private val scope: CoroutineScope,
    private val dispatchers: DispatchersProvider
) : ChatInteractor {

    private var eventsJob: Job? = null

    override fun initialize() {
        scope.launch(dispatchers.io) {
            authInteractor.authState.collect {
                when (it) {
                    is AuthState.None -> onAuthFailure()
                    is AuthState.Authenticated -> onAuthSuccess()
                }
            }
        }
    }

    override fun observeChats(): Flow<Resource<List<Chat>>> {
        return chatRepository.observeChatsFromDatabase()
    }

    override fun observeMessagesForChat(interlocutor: User): Flow<List<Message>> {
        return chatRepository.observeMessagesForChatFromDatabase(interlocutor)
    }

    override suspend fun fetchNextPortionOfMessagesForChat(interlocutor: User): LoadResult {
        return chatRepository.fetchNextPortionOfMessagesForChat(interlocutor)
    }

    override suspend fun sendMessage(body: String?, image: String?, to: User) {
        if (body.isNullOrBlank() && image == null) return

        val localImage = image?.let {
            LocalImage(it, imagesInteractor.compressImage(it))
        }
        chatRepository.sendMessage(body, localImage, to)
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
                .catch { log("ChatInteractorImpl.startObservingEvents() $it") }
                .onEach { log("ChatInteractorImpl.startObservingEvents().onEach() $it") }
                .flowOn(dispatchers.io)
                .collect {
                    when (it) {
                        is Event.Connect -> onConnect()
                        is Event.Disconnect -> onDisconnect()
                        is Event.NewMessage -> onNewMessage(it.message)
                        is Event.NewChat -> onNewChat(it.chat)
                    }
                }
        }
        chatRepository.sendDeviceTokenIfNeeded()
    }

    private fun onAuthFailure() {
        eventsJob?.cancel()
    }

    private fun onConnect() {

    }

    private fun onDisconnect() {

    }

    private suspend fun onNewMessage(message: Message) {
        chatRepository.addNewMessage(message)
        notificationInteractor.showMessageNotificationIfNeeded(message)
    }

    private suspend fun onNewChat(chat: Chat) {
        chatRepository.addNewChat(chat)
        notificationInteractor.showMessageNotificationIfNeeded(chat.lastMessage)
    }
}