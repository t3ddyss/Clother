package com.t3ddyss.clother.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.domain.chat.ChatInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import javax.inject.Inject

@HiltViewModel
class ChatsListViewModel @Inject constructor(
    chatInteractor: ChatInteractor
) : ViewModel() {

    val chats = chatInteractor
        .observeChats()
        .shareIn(viewModelScope, SharingStarted.Eagerly)
}