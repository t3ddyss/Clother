package com.t3ddyss.clother.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.Mappers.toEntity
import com.t3ddyss.clother.models.domain.LoadResult
import com.t3ddyss.clother.models.domain.LoadType
import com.t3ddyss.clother.models.entity.RemoteKeyEntity
import com.t3ddyss.clother.remote.RemoteChatService
import com.t3ddyss.clother.util.ACCESS_TOKEN
import com.t3ddyss.clother.util.CLOTHER_PAGE_SIZE_CHAT
import com.t3ddyss.core.domain.models.User
import com.t3ddyss.core.util.log

class MessagesPagingLoader(
    private val service: RemoteChatService,
    private val prefs: SharedPreferences,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val remoteKeyDao: RemoteKeyDao,
    private val listKey: String,
    private val interlocutor: User
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
                accessToken = prefs.getString(ACCESS_TOKEN, null),
                afterKey = key,
                beforeKey = null,
                limit = CLOTHER_PAGE_SIZE_CHAT
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
            log("${this.javaClass.simpleName} $ex")
            LoadResult.Error(ex)
        }
    }
}