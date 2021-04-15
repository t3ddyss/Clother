package com.t3ddyss.clother.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.LiveMessagesRepository
import com.t3ddyss.clother.data.MessagesRepository
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.common.LoadResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class ChatViewModel @Inject constructor(
        private val repository: MessagesRepository,
        private val liveRepository: LiveMessagesRepository,
): ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _loadStatus = MutableLiveData<LoadResult>()
    val loadStatus: LiveData<LoadResult> = _loadStatus

    @Volatile
    var isLoading = AtomicBoolean(false)
    var isEndOfPaginationReached = false

    fun getMessages(interlocutorId: Int) {
        if (messages.value != null) return

        viewModelScope.launch {
            repository.getMessages(interlocutorId).collectLatest {
                _messages.postValue(it)
            }
        }

        getMoreMessages(interlocutorId)
    }

    fun getMoreMessages(interlocutorId: Int) {
        if (isEndOfPaginationReached || isLoading.getAndSet(true)) return

        viewModelScope.launch {
            _loadStatus.postValue(repository.fetchMessages(interlocutorId))
            isLoading.set(false)
        }
    }

    fun sendMessage(message: String, to: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            liveRepository.sendMessage(to, message)
        }
    }
}