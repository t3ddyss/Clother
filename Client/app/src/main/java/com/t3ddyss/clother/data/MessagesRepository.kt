package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.common.LoadResult
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@ViewModelScoped
class MessagesRepository @Inject constructor(
        private val service: ClotherChatService,
        private val prefs: SharedPreferences,
        private val db: AppDatabase,
        private val chatDao: ChatDao,
        private val messageDao: MessageDao,
        private val remoteKeyDao: RemoteKeyDao,
) {
//    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var messageLoader: MessagesPagingLoader

    fun getMessages(interlocutorId: Int) = messageDao
            .getMessagesByInterlocutorId(interlocutorId)

    suspend fun fetchMessages(interlocutorId: Int): LoadResult {
        if (!this@MessagesRepository::messageLoader.isInitialized) {
            messageLoader = MessagesPagingLoader(
                    service = service,
                    prefs = prefs,
                    db = db,
                    chatDao = chatDao,
                    messageDao = messageDao,
                    remoteKeyDao = remoteKeyDao,
                    remoteKeyList = KEY_LIST,
                    interlocutorId = interlocutorId
            )
        }

        return messageLoader.load()
    }

    companion object {
        const val KEY_LIST = "messages"
    }
}