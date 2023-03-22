package com.t3ddyss.clother.domain.chat

import android.net.Uri
import arrow.core.Either
import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.ChatsState
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.core.domain.models.ApiCallError
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeChatsFromDatabase(): Flow<ChatsState>
    fun observeMessagesForChatFromDatabase(interlocutorId: Int): Flow<List<Message>>
    suspend fun fetchNextPortionOfMessagesForChat(interlocutorId: Int): LoadResult
    suspend fun sendMessage(body: String?, image: Uri?, interlocutorId: Int)
    suspend fun retryToSendMessage(messageLocalId: Int)
    suspend fun deleteMessage(messageLocalId: Int): Either<ApiCallError, Unit>
    suspend fun addNewMessage(message: Message)
    suspend fun addNewChat(chat: Chat)
    suspend fun removeMessage(messageId: Int)
    suspend fun sendDeviceTokenIfNeeded()
    suspend fun sendDeviceToken(token: String)
}