package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.domain.LoadResult
import com.t3ddyss.clother.models.mappers.mapMessageEntityToDomain
import com.t3ddyss.core.domain.User
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.map
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
    private lateinit var messageLoader: MessagesPagingLoader

    fun observeMessages(interlocutor: User) = messageDao
        .observeMessagesByInterlocutorId(interlocutor.id)
        .map { messages ->
            messages.map {
                mapMessageEntityToDomain(it, it.userId == interlocutor.id)
            }
        }

    suspend fun fetchMessages(interlocutor: User): LoadResult {
        if (!this@MessagesRepository::messageLoader.isInitialized) {
            messageLoader = MessagesPagingLoader(
                service = service,
                prefs = prefs,
                db = db,
                chatDao = chatDao,
                messageDao = messageDao,
                remoteKeyDao = remoteKeyDao,
                listKey = LIST_KEY_MESSAGES + interlocutor.id,
                interlocutor = interlocutor
            )
        }

        return messageLoader.load()
    }

    companion object {
        const val LIST_KEY_MESSAGES = "messages"
    }
}