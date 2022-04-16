package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.models.LoadResult
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.User
import kotlinx.coroutines.flow.Flow

interface ChatInteractor {
    fun observeChats(): Flow<Resource<List<Chat>>>
    fun observeMessagesForChat(interlocutor: User): Flow<List<Message>>
    suspend fun fetchNextPortionOfMessagesForChat(interlocutor: User): LoadResult
}