package com.t3ddyss.clother.presentation.chat

import androidx.lifecycle.*
import com.t3ddyss.clother.domain.chat.ChatInteractor
import com.t3ddyss.clother.domain.chat.NotificationInteractor
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.domain.common.navigation.NavigationInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val isLoading = AtomicBoolean(false)
    var isEndOfPaginationReached = false

    init {
        viewModelScope.launch {
            notificationInteractor.cancelMessageNotifications(interlocutor.id)
            chatInteractor.observeMessagesForChat(interlocutor)
                .collectLatest {
                    _messages.postValue(it)
            }
        }

        navigationInteractor.interlocutorId = interlocutor.id
        requestMessages()
    }

    fun requestMessages() {
        if (isEndOfPaginationReached || isLoading.getAndSet(true)) return

        viewModelScope.launch {
            _loadStatus.postValue(chatInteractor.fetchNextPortionOfMessagesForChat(interlocutor))
            isLoading.set(false)
        }
    }

    fun sendMessage(body: String? = null, image: String? = null) {
        viewModelScope.launch {
            chatInteractor.sendMessage(body, image, interlocutor)
        }
    }
}