package com.t3ddyss.clother.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.t3ddyss.clother.data.MessagesRepository
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.offers.Offer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@ExperimentalPagingApi
@ExperimentalCoroutinesApi
class ChatViewModel
@Inject constructor(
        private val repository: MessagesRepository,
): ViewModel() {
    private val _messages = MutableLiveData<PagingData<Message>>()
    val offers: LiveData<PagingData<Message>> = _messages

    var endOfPaginationReachedBottom = false

    fun getMessages(interlocutorId: Int) {
        viewModelScope.launch {
            repository
                    .getMessages(interlocutorId, REMOTE_KEY_CHAT)
                    .cachedIn(viewModelScope)
                    .collectLatest {
                        _messages.postValue(it)
                    }
        }
    }

    companion object {
        const val REMOTE_KEY_CHAT = "chat"
    }
}