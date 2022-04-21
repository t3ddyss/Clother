package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.User
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ChatRepository {
    fun observeChatsFromDatabase(): Flow<Resource<List<Chat>>>
    fun observeMessagesForChatFromDatabase(interlocutor: User): Flow<List<Message>>
    suspend fun fetchNextPortionOfMessagesForChat(interlocutor: User): LoadResult
    suspend fun sendMessage(body: String, image: File?, interlocutor: User)
    suspend fun addNewMessage(message: Message)
    suspend fun addNewChat(chat: Chat)
    suspend fun sendDeviceTokenIfNeeded()
    suspend fun sendDeviceToken(token: String)
}