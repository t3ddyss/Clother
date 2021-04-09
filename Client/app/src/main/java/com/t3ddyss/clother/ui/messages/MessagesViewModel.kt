package com.t3ddyss.clother.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class MessagesViewModel @Inject constructor(
    private val repository: MessagesRepository
) : ViewModel() {
    private val _messages = MutableLiveData<String>()
    val messages: LiveData<String> = _messages

    fun getMessages() {
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