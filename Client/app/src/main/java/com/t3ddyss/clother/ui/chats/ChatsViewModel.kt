package com.t3ddyss.clother.ui.chats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.ChatsRepository
import com.t3ddyss.clother.models.domain.ChatWithLastMessage
import com.t3ddyss.clother.models.domain.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: ChatsRepository
) : ViewModel() {
    private val _chats = MutableLiveData<Resource<List<ChatWithLastMessage>>>()
    val chats: LiveData<Resource<List<ChatWithLastMessage>>> = _chats
    private val isChatsLoaded = AtomicBoolean(false)

    fun getChats() {
        if (isChatsLoaded.getAndSet(true)) return

        viewModelScope.launch {
            repository.getChats().collectLatest {
                _chats.postValue(it)
            }
        }
    }
}