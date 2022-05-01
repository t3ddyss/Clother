package com.t3ddyss.clother.presentation.chat

import androidx.lifecycle.*
import com.t3ddyss.clother.domain.chat.ChatInteractor
import com.t3ddyss.clother.domain.chat.NotificationInteractor
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.domain.common.navigation.NavigationInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatInteractor: ChatInteractor,
    navigationInteractor: NavigationInteractor,
    notificationInteractor: NotificationInteractor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val args = ChatFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val interlocutor = args.user

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _loadStatus = MutableLiveData<LoadResult>()
    val loadStatus: LiveData<LoadResult> = _loadStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val isLoadingMessages = AtomicBoolean(false)
    private var isEndOfPaginationReached = false

    init {
        viewModelScope.launch {
            notificationInteractor.cancelMessageNotifications(interlocutor.id)
            chatInteractor.observeMessagesForChat(interlocutor)
                .collectLatest {
                    _messages.postValue(it)
            }
        }

        navigationInteractor.interlocutorId = interlocutor.id
        fetchNextPortionOfMessages()
    }

    private fun fetchNextPortionOfMessages() {
        viewModelScope.launch {
            val result = chatInteractor.fetchNextPortionOfMessagesForChat(interlocutor)
            isLoadingMessages.set(false)
            isEndOfPaginationReached = (result as? LoadResult.Success)?.isEndOfPaginationReached ?: false
            _loadStatus.postValue(result)
        }
    }

    fun onListEndReached() {
        if (!isEndOfPaginationReached && !isLoadingMessages.getAndSet(true)) {
            fetchNextPortionOfMessages()
        }
    }

    fun sendMessage(body: String? = null, image: String? = null) {
        viewModelScope.launch {
            chatInteractor.sendMessage(body, image, interlocutor)
        }
    }

    fun retryToSendMessage(message: Message) {
        viewModelScope.launch {
            chatInteractor.retryToSendMessage(message)
        }
    }

    fun deleteMessage(message: Message) {
        val loadingStateJob = viewModelScope.launch {
            delay(DELETION_LOADING_DELAY_IN_MILLIS)
            _isLoading.postValue(true)
        }
        viewModelScope.launch {
            chatInteractor.deleteMessage(message)
            loadingStateJob.cancel()
            _isLoading.postValue(false)
        }
    }

    private companion object {
        const val DELETION_LOADING_DELAY_IN_MILLIS = 250L
    }
}