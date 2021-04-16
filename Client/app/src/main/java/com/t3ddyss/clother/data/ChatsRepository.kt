package com.t3ddyss.clother.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.networkBoundResource
import javax.inject.Inject

class ChatsRepository @Inject constructor(
    private val service: ClotherChatService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val prefs: SharedPreferences
) {
    fun getChats() = networkBoundResource(
            query = { chatDao.getAllChats() },
            fetch = { service.getChats(prefs.getString(ACCESS_TOKEN, null)) },
            saveFetchResult = { db.withTransaction {
                chatDao.deleteRemovedChats(it.map { it.serverId?.toLong() ?: 0 }.toTypedArray())
                messageDao.deleteUnsendMessages()
                chatDao.insertAll(it)
            }
            }
    )
}