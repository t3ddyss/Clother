package com.t3ddyss.clother.domain.chat

import android.net.Uri
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.CloudEvent
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.core.domain.models.Resource
import kotlinx.coroutines.flow.Flow

interface ChatInteractor {
    fun initialize()
    fun observeChats(): Flow<Resource<List<Chat>>>
    fun observeMessagesForChat(interlocutorId: Int): Flow<List<Message>>
    suspend fun fetchNextPortionOfMessagesForChat(interlocutorId: Int): LoadResult
    suspend fun sendMessage(body: String?, image: Uri?, interlocutorId: Int)
    suspend fun retryToSendMessage(messageLocalId: Int)
    suspend fun deleteMessage(messageLocalId: Int): Resource<*>
    fun onNewToken(token: String)
    fun onNewCloudEvent(cloudEvent: CloudEvent)
}