package com.t3ddyss.clother.presentation.chat

import androidx.lifecycle.*
import com.t3ddyss.clother.data.LiveMessagingRepository
import com.t3ddyss.clother.data.MessagesRepository
import com.t3ddyss.clother.domain.models.LoadResult
import com.t3ddyss.clother.domain.models.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: MessagesRepository,
    private val liveRepository: LiveMessagingRepository,
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
            repository.observeMessages(interlocutor).collectLatest {
                _messages.postValue(it)
            }
        }

        liveRepository.setCurrentInterlocutorId(interlocutor.id)
        requestMessages()
    }

    fun requestMessages() {
        if (isEndOfPaginationReached || isLoading.getAndSet(true)) return

        viewModelScope.launch {
            _loadStatus.postValue(repository.fetchMessages(interlocutor))
            isLoading.set(false)
        }
    }

    fun sendMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            liveRepository.sendMessage(interlocutor, message)
        }
    }
}