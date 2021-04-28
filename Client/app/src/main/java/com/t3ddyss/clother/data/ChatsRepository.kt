package com.t3ddyss.clother.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.models.mappers.mapChatDtoToEntity
import com.t3ddyss.clother.models.mappers.mapMessageDtoToEntity
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
        saveFetchResult = {
            db.withTransaction {
                chatDao.deleteUncreatedChats(
                    it.map { it.id.toLong() }.toTypedArray()
                )
                messageDao.deleteUnsentMessages()

                val ids = chatDao.insertAll(
                    it.map { mapChatDtoToEntity(it) }
                )

                messageDao.insertAll(
                    ids
                        .zip(it)
                        .map { chatWithId ->
                            mapMessageDtoToEntity(chatWithId.second.lastMessage).also {
                                it.localChatId = chatWithId.first.toInt()
                            }
                        }
                )
            }
        }
    )
}