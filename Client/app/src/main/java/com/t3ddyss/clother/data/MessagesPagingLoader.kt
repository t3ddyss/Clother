package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.util.Log
import androidx.room.withTransaction
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.common.LoadResult
import com.t3ddyss.clother.models.common.LoadType
import com.t3ddyss.clother.models.common.RemoteKey
import com.t3ddyss.clother.models.user.User
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.CLOTHER_PAGE_SIZE_CHAT
import com.t3ddyss.clother.utilities.DEBUG_TAG

class MessagesPagingLoader (
        private val service: ClotherChatService,
        prefs: SharedPreferences,
        private val db: AppDatabase,
        private val chatDao: ChatDao,
        private val messageDao: MessageDao,
        private val remoteKeyDao: RemoteKeyDao,
        private val remoteKeyList: String,
        private val interlocutor: User
) {
    private var accessToken: String? = null
    private var changeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
                run {
                    if (key == ACCESS_TOKEN) {
                        accessToken = sp.getString(key, null)
                    }
                }
            }

    private var loadType = LoadType.REFRESH

    init {
        accessToken = prefs.getString(ACCESS_TOKEN, null)
        prefs.registerOnSharedPreferenceChangeListener(changeListener)
    }

    suspend fun load(): LoadResult {

        val key = when (loadType) {
            LoadType.REFRESH -> {
                null
            }

            LoadType.APPEND -> {
                val afterKey = db.withTransaction {
                    remoteKeyDao.remoteKeyByList(remoteKeyList + interlocutor.id).afterKey
                }

                afterKey
            }
        }

        return try {
            val items = service.getMessages(
                    interlocutorId = interlocutor.id,
                    accessToken = accessToken,
                    afterKey = key,
                    beforeKey = null,
                    limit = CLOTHER_PAGE_SIZE_CHAT)

            db.withTransaction {
                // TODO handle situation when user opens existing chat which is not cached from offer fragment
                val chat = chatDao.getChatByInterlocutorId(interlocutor.id)

                if (chat != null && loadType == LoadType.REFRESH) {
                    messageDao.deleteAllMessagesFromChat(chat.localId)
                    remoteKeyDao.removeByList(remoteKeyList + interlocutor.id)
                    chatDao.insert(chat.copy(lastMessage = items.first()))

                    loadType = LoadType.APPEND
                }


                messageDao.insertAll(items)
                remoteKeyDao.insert(RemoteKey(
                        remoteKeyList + interlocutor.id,
                        items.lastOrNull()?.serverId))
            }

            LoadResult.Success(isEndOfPaginationReached = items.isEmpty())
        } catch (ex: Exception) {
            Log.d(DEBUG_TAG, "MessagesPagingLoader $ex")
            LoadResult.Error(ex)
        }
    }
}