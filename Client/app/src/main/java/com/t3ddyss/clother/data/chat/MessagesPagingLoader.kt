package com.t3ddyss.clother.data.chat

import androidx.room.withTransaction
import com.t3ddyss.clother.data.chat.db.ChatDao
import com.t3ddyss.clother.data.chat.db.MessageDao
import com.t3ddyss.clother.data.chat.remote.RemoteChatService
import com.t3ddyss.clother.data.common.common.Mappers.toEntity
import com.t3ddyss.clother.data.common.common.Storage
import com.t3ddyss.clother.data.common.common.db.AppDatabase
import com.t3ddyss.clother.data.offers.db.RemoteKeyDao
import com.t3ddyss.clother.data.offers.db.models.RemoteKeyEntity
import com.t3ddyss.clother.domain.common.common.models.LoadResult
import com.t3ddyss.clother.domain.common.common.models.LoadType
import com.t3ddyss.core.util.log
import javax.inject.Inject

class MessagesPagingLoader @Inject constructor(
    private val service: RemoteChatService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val storage: Storage
) {
    private val interlocutorsLoadTypes = mutableMapOf<Int, LoadType>()

    suspend fun load(listKey: String, interlocutorId: Int): LoadResult {
        val loadType = interlocutorsLoadTypes[interlocutorId] ?: LoadType.REFRESH
        val key = when (loadType) {
            LoadType.REFRESH -> {
                null
            }

            LoadType.APPEND -> {
                val afterKey = db.withTransaction {
                    remoteKeyDao.remoteKeyByList(listKey).afterKey
                }
                afterKey
            }
        }

        return try {
            val items = service.getMessages(
                interlocutorId = interlocutorId,
                accessToken = storage.accessToken,
                afterKey = key,
                beforeKey = null,
                limit = PAGE_SIZE
            )

            db.withTransaction {
                // TODO handle situation when user opens existing chat which is not cached yet from
                //  offer fragment
                val chat = chatDao.getChatByInterlocutorId(interlocutorId)

                if (chat != null && loadType == LoadType.REFRESH) {
                    messageDao.deleteAllMessagesFromChat(chat.serverId)
                    remoteKeyDao.removeByList(listKey)
                    interlocutorsLoadTypes[interlocutorId] = LoadType.APPEND
                }

                if (chat != null) {
                    messageDao.insertAll(
                        items.map { messageDto ->
                            messageDto.toEntity().also {
                                it.localChatId = chat.localId
                            }
                        }
                    )
                }
                remoteKeyDao.insert(
                    RemoteKeyEntity(
                        listKey,
                        items.lastOrNull()?.id
                    )
                )
            }

            LoadResult.Success(isEndOfPaginationReached = items.isEmpty())
        } catch (ex: Exception) {
            log("MessagesPagingLoader.load() $ex")
            LoadResult.Error(ex)
        }
    }

    private companion object {
        const val PAGE_SIZE = 25
    }
}