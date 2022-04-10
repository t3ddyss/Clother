package com.t3ddyss.clother.data

import androidx.room.withTransaction
import com.t3ddyss.clother.data.Mappers.toEntity
import com.t3ddyss.clother.data.db.AppDatabase
import com.t3ddyss.clother.data.db.ChatDao
import com.t3ddyss.clother.data.db.MessageDao
import com.t3ddyss.clother.data.db.RemoteKeyDao
import com.t3ddyss.clother.data.db.entity.RemoteKeyEntity
import com.t3ddyss.clother.data.remote.RemoteChatService
import com.t3ddyss.clother.domain.models.LoadResult
import com.t3ddyss.clother.domain.models.LoadType
import com.t3ddyss.core.domain.models.User
import com.t3ddyss.core.util.log
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MessagesPagingLoader @AssistedInject constructor(
    private val service: RemoteChatService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val storage: Storage,
    @Assisted private val listKey: String,
    @Assisted private val interlocutor: User
) {
    private var loadType = LoadType.REFRESH

    suspend fun load(): LoadResult {
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
                interlocutorId = interlocutor.id,
                accessToken = storage.accessToken,
                afterKey = key,
                beforeKey = null,
                limit = PAGE_SIZE
            )

            db.withTransaction {
                // TODO handle situation when user opens existing chat which is not cached yet from
                //  offer fragment
                val chat = chatDao.getChatByInterlocutorId(interlocutor.id)

                if (chat != null && loadType == LoadType.REFRESH) {
                    messageDao.deleteAllMessagesFromChat(chat.serverId)
                    remoteKeyDao.removeByList(listKey)

                    loadType = LoadType.APPEND
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

@AssistedFactory
interface MessagesPagingLoaderFactory {
    fun create(listKey: String, interlocutor: User): MessagesPagingLoader
}