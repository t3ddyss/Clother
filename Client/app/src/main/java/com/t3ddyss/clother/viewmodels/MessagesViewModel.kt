package com.t3ddyss.clother.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.LiveMessagesRepository
import com.t3ddyss.clother.data.LiveMessagesRepository.Companion.CONNECTED
import com.t3ddyss.clother.data.MessagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalCoroutinesApi
class MessagesViewModel @Inject constructor(
    private val liveRepository: LiveMessagesRepository,
    private val repository: MessagesRepository,
    private val prefs: SharedPreferences
) : ViewModel() {
    private var messagesJob: Job? = null
    private var isConnecting = false

    fun getMessages(tokenUpdated: Boolean = false) {
        if (isConnecting || liveRepository.isConnected) {
            if (!tokenUpdated) return

            liveRepository.disconnectFromServer()
            messagesJob?.cancel()
        }
        isConnecting = true

        messagesJob = viewModelScope.launch {
            liveRepository.getMessagesStream().collect {
                if (it == CONNECTED) {
                    isConnecting = false
                }
            }
        }
    }

    fun sendDeviceTokenToServer() {
        viewModelScope.launch {
            repository.sendDeviceTokenToServer()
        }
    }

    fun setIsChatsDestination(isChatsFragment: Boolean, isChatFragment: Boolean) {
        liveRepository.isChatsFragment = isChatsFragment

        if (!isChatFragment) {
            liveRepository.currentInterlocutorId = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        liveRepository.disconnectFromServer()
    }
}