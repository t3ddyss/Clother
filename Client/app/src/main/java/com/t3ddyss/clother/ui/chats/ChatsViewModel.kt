package com.t3ddyss.clother.ui.chats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.t3ddyss.clother.data.ChatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: ChatsRepository
): ViewModel() {
    val chats = liveData {
        emit(repository.getChats())
    }
}