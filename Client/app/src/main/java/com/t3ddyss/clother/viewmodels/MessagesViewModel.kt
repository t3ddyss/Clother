package com.t3ddyss.clother.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.LiveMessagesRepository
import com.t3ddyss.clother.models.chat.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class MessagesViewModel @Inject constructor(
    private val repository: LiveMessagesRepository
) : ViewModel() {
    private val _messages = MutableLiveData<Message>()
    val messages: LiveData<Message> = _messages

    fun getMessages() {
        if (repository.isConnected) return

        viewModelScope.launch {
            repository.getMessagesStream().collect {
                _messages.postValue(it)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnectFromServer()
    }
}