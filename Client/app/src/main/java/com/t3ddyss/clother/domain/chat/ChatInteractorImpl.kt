package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Chat
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.models.LoadResult
import com.t3ddyss.core.domain.models.Resource
import com.t3ddyss.core.domain.models.User
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatInteractorImpl @Inject constructor(
    private val chatRepository: ChatRepository
) : ChatInteractor {

    override fun observeChats(): Flow<Resource<List<Chat>>> {
        return chatRepository.observeChatsFromDatabase()
    }

    override fun observeMessagesForChat(interlocutor: User): Flow<List<Message>> {
        return chatRepository.observeMessagesForChatFromDatabase(interlocutor)
    }

    override suspend fun fetchNextPortionOfMessagesForChat(interlocutor: User): LoadResult {
        return chatRepository.fetchNextPortionOfMessagesForChat(interlocutor)
    }
}