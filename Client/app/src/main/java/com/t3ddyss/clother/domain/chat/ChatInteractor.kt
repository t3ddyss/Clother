package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.CloudEvent
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.User
import kotlinx.coroutines.flow.Flow

interface ChatInteractor {
    fun initialize()
    fun observeChats(): Flow<Resource<List<Chat>>>
    fun observeMessagesForChat(interlocutor: User): Flow<List<Message>>
    suspend fun fetchNextPortionOfMessagesForChat(interlocutor: User): LoadResult
    suspend fun sendMessage(body: String?, image: String?, to: User)
    fun onNewToken(token: String)
    fun onNewCloudEvent(cloudEvent: CloudEvent)
}