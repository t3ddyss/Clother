package com.t3ddyss.clother.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.LiveMessagesRepository
import com.t3ddyss.clother.data.MessagesRepository
import com.t3ddyss.clother.models.chat.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class MessagesViewModel @Inject constructor(
    private val liveRepository: LiveMessagesRepository,
    private val repository: MessagesRepository
) : ViewModel() {
    private val _messages = MutableLiveData<Message>()
    val messages: LiveData<Message> = _messages

    fun getMessages() {
        if (liveRepository.isConnected) return

        viewModelScope.launch {
            liveRepository.getMessagesStream().collect {
                _messages.postValue(it)
            }
        }
    }

    fun sendDeviceTokenToServer() {
        viewModelScope.launch {
            repository.sendDeviceTokenToServer()
        }
    }

    override fun onCleared() {
        super.onCleared()
        liveRepository.disconnectFromServer()
    }
}