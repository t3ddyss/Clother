package com.t3ddyss.clother.data.chat

import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.common.Mappers.toDomain
import com.t3ddyss.clother.domain.common.models.LoadResult
import com.t3ddyss.core.domain.models.User
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@ViewModelScoped
class MessagesRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val messagesPagingLoaderFactory: MessagesPagingLoaderFactory
) {
    private var messagesPagingLoader: MessagesPagingLoader? = null

    fun observeMessages(interlocutor: User) = messageDao
        .observeMessagesByInterlocutorId(interlocutor.id)
        .map { messages ->
            messages.map {
                it.toDomain(it.userId == interlocutor.id)
            }
        }

    suspend fun fetchMessages(interlocutor: User): LoadResult {
        if (messagesPagingLoader == null) {
            val listKey = LIST_KEY_MESSAGES + interlocutor.id
            messagesPagingLoader = messagesPagingLoaderFactory.create(listKey, interlocutor)
        }

        return messagesPagingLoader!!.load()
    }

    companion object {
        const val LIST_KEY_MESSAGES = "messages"
    }
}