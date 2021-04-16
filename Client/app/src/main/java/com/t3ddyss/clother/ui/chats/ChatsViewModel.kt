package com.t3ddyss.clother.ui.chats

import androidx.lifecycle.*
import com.t3ddyss.clother.data.ChatsRepository
import com.t3ddyss.clother.data.LiveMessagesRepository
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: ChatsRepository,
    private val liveRepository: LiveMessagesRepository
): ViewModel() {
    private val _chats = MutableLiveData<Resource<List<Chat>>>()
    val chats: LiveData<Resource<List<Chat>>> = _chats
    var isChatsLoaded = AtomicBoolean(false)

    fun getChats() {
        if (isChatsLoaded.getAndSet(true)) return

        viewModelScope.launch {
            repository.getChats().collectLatest {
                _chats.postValue(it)
            }
        }
    }
}