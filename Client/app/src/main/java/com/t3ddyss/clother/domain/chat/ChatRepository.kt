package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.LocalImage
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.core.domain.models.Resource
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChatsFromDatabase(): Flow<Resource<List<Chat>>>
    fun observeMessagesForChatFromDatabase(interlocutorId: Int): Flow<List<Message>>
    suspend fun fetchNextPortionOfMessagesForChat(interlocutorId: Int): LoadResult
    suspend fun sendMessage(body: String?, image: LocalImage?, interlocutorId: Int)
    suspend fun retryToSendMessage(message: Message, image: LocalImage?)
    suspend fun deleteMessage(message: Message)
    suspend fun addNewMessage(message: Message)
    suspend fun addNewChat(chat: Chat)
    suspend fun removeMessage(messageId: Int)
    suspend fun sendDeviceTokenIfNeeded()
    suspend fun sendDeviceToken(token: String)
}