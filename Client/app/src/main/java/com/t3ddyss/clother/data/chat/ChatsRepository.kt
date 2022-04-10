package com.t3ddyss.clother.data.chat

import androidx.room.withTransaction
import com.t3ddyss.clother.data.chat.db.ChatDao
import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.chat.remote.RemoteChatService
import com.t3ddyss.clother.data.common.Mappers.toDomain
import com.t3ddyss.clother.data.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.Storage
import com.t3ddyss.clother.data.common.db.AppDatabase
import com.t3ddyss.clother.util.networkBoundResource
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatsRepository @Inject constructor(
    private val service: RemoteChatService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val storage: Storage
) {
    fun observeChats() = networkBoundResource(
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
}