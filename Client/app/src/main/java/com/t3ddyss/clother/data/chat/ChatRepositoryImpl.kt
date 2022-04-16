package com.t3ddyss.clother.data.chat

import androidx.room.withTransaction
import com.t3ddyss.clother.data.chat.db.ChatDao
import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.chat.remote.RemoteChatService
import com.t3ddyss.clother.data.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.Storage
import com.t3ddyss.clother.data.common.db.AppDatabase
import com.t3ddyss.clother.domain.chat.ChatRepository
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.models.LoadResult
import com.t3ddyss.clother.util.networkBoundResource
import com.t3ddyss.core.domain.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val service: RemoteChatService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val messagesPagingLoader: MessagesPagingLoader,
    private val storage: Storage
) : ChatRepository {

    override fun observeChatsFromDatabase() = networkBoundResource(
        query = {
            chatDao.observeChats().map {
                    chats -> chats.map { it.toDomain() }
            }
        },
        fetch = { service.getChats(storage.accessToken) },
        saveFetchResult = {
            db.withTransaction {
                chatDao.deleteUncreatedChats(
                    it.map { it.id.toLong() }.toTypedArray()
                )
                messageDao.deleteUnsentMessages()

                val ids = chatDao.insertAll(
                    it.map { it.toEntity() }
                )

                messageDao.insertAll(
                    ids
                        .zip(it)
                        .map { chatWithId ->
                            chatWithId.second.lastMessage.toEntity().also {
                                it.localChatId = chatWithId.first.toInt()
                            }
                        }
                )
            }
        }
    )

    override fun observeMessagesForChatFromDatabase(interlocutor: User): Flow<List<Message>> {
        return messageDao
            .observeMessagesByInterlocutorId(interlocutor.id)
            .map { messages ->
                messages.map {
                    it.toDomain(it.userId == interlocutor.id)
                }
            }
    }

    override suspend fun fetchNextPortionOfMessagesForChat(interlocutor: User): LoadResult {
        return messagesPagingLoader.load(
            listKey = LIST_KEY_MESSAGES + interlocutor.id,
            interlocutorId = interlocutor.id
        )
    }

    private companion object {
        const val LIST_KEY_MESSAGES = "messages"
    }
}