package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.*
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.common.RemoteKey
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.DEBUG_TAG
import java.lang.Exception

@ExperimentalPagingApi
class MessagesRemoteMediator(
        private val service: ClotherChatService,
        prefs: SharedPreferences,
        private val db: AppDatabase,
        private val chatDao: ChatDao,
        private val messageDao: MessageDao,
        private val remoteKeyDao: RemoteKeyDao,
        private val remoteKeyList: String,
        private val interlocutorId: Int
) : RemoteMediator<Int, Message>() {
    private var accessToken: String? = null
    private var changeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                run {
                    if (key == ACCESS_TOKEN) {
                        accessToken = sp.getString(key, null)
                    }
                }
            }

    init {
        accessToken = prefs.getString(ACCESS_TOKEN, null)
        prefs.registerOnSharedPreferenceChangeListener(changeListener)
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Message>): MediatorResult {
        val key: Int? = when (loadType) {
            LoadType.REFRESH -> {
                Log.d(DEBUG_TAG, "REFRESH")

                null
            }

            LoadType.PREPEND -> {
                Log.d(DEBUG_TAG, "PREPEND")

                return MediatorResult.Success(endOfPaginationReached = true)
            }

            LoadType.APPEND -> {
                val afterKey = db.withTransaction {
                    remoteKeyDao.remoteKeyByList(remoteKeyList + interlocutorId).afterKey
                }

                Log.d(DEBUG_TAG, "APPEND ${afterKey ?: "NULL"}")

                afterKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        return try {
            val items = service.getMessages(
                    interlocutorId = interlocutorId,
                    accessToken = accessToken,
                    afterKey = key,
                    beforeKey = null,
                    limit = when (loadType) {
                        LoadType.REFRESH -> state.config.initialLoadSize
                        else -> state.config.pageSize
                    })

            db.withTransaction {
                val chatId = chatDao.getChatIdByInterlocutorId(interlocutorId)

                if (loadType == LoadType.REFRESH) {
                    messageDao.deleteAllMessagesFromChat(chatId)
                    remoteKeyDao.removeByList(remoteKeyList + interlocutorId)
                }

                // TODO update "latest" message for corresponding chat
                messageDao.insertAll(items)
                remoteKeyDao.insert(RemoteKey(
                        remoteKeyList + interlocutorId,
                        items.lastOrNull()?.id))
            }

            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (ex: Exception) {
            Log.d(DEBUG_TAG, "Mediator $ex")
            MediatorResult.Error(ex)
        }
    }
}