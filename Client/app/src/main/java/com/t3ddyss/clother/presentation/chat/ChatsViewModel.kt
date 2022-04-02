package com.t3ddyss.clother.presentation.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.t3ddyss.clother.data.ChatsRepository
import com.t3ddyss.clother.domain.models.Chat
import com.t3ddyss.core.domain.models.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val repository: ChatsRepository
) : ViewModel() {
    private val _chats = MutableLiveData<Resource<List<Chat>>>()
    val chats: LiveData<Resource<List<Chat>>> = _chats

    init {
        viewModelScope.launch {
            repository.observeChats().collectLatest {
                _chats.postValue(it)
            }
        }
    }
}