package com.t3ddyss.clother.ui.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.LiveMessagesRepository
import com.t3ddyss.clother.data.MessagesRepository
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.common.LoadResult
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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

    fun getMessages(interlocutorId: Int) {
        viewModelScope.launch {
            repository.getMessages(interlocutorId).collectLatest {
                Log.d(DEBUG_TAG, "Collected messages")
                _messages.postValue(it)
            }
        }

        viewModelScope.launch {
            _loadStatus.postValue(repository.fetchMessages(interlocutorId))
        }
    }

    fun sendMessage(message: String, to: Int = 1) {
        viewModelScope.launch(Dispatchers.IO) {
            liveRepository.sendMessage(to, message)
        }
    }
}