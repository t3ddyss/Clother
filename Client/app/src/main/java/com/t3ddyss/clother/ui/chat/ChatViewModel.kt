package com.t3ddyss.clother.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.LiveMessagesRepository
import com.t3ddyss.clother.data.MessagesRepository
import com.t3ddyss.clother.models.domain.LoadResult
import com.t3ddyss.clother.models.domain.Message
import com.t3ddyss.clother.models.domain.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: MessagesRepository,
    private val liveRepository: LiveMessagesRepository,
) : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _loadStatus = MutableLiveData<LoadResult>()
    val loadStatus: LiveData<LoadResult> = _loadStatus

    var isLoading = AtomicBoolean(false)
    var isEndOfPaginationReached = false

    fun getMessages(interlocutor: User) {
        if (messages.value != null) return

        viewModelScope.launch {
            repository.getMessages(interlocutor).collectLatest {
                _messages.postValue(it)
            }
        }

        liveRepository.setCurrentInterlocutor(interlocutor.id)
        getMoreMessages(interlocutor)
    }

    fun getMoreMessages(interlocutor: User) {
        if (isEndOfPaginationReached || isLoading.getAndSet(true)) return

        viewModelScope.launch {
            _loadStatus.postValue(repository.fetchMessages(interlocutor))
            isLoading.set(false)
        }
    }

    fun sendMessage(message: String, to: User) {
        viewModelScope.launch(Dispatchers.IO) {
            liveRepository.sendMessage(to, message)
        }
    }
}