package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessaging
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.domain.LoadResult
import com.t3ddyss.clother.models.domain.User
import com.t3ddyss.clother.models.mappers.mapMessageEntityToDomain
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.IS_DEVICE_TOKEN_RETRIEVED
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.resume

@ViewModelScoped
class MessagesRepository @Inject constructor(
    private val service: ClotherChatService,
    private val authService: ClotherAuthService,
    private val prefs: SharedPreferences,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val remoteKeyDao: RemoteKeyDao,
) {
    private lateinit var messageLoader: MessagesPagingLoader

    fun getMessages(interlocutor: User) = messageDao
        .getMessagesByInterlocutorId(interlocutor.id)
        .map { messages ->
            messages.map {
                mapMessageEntityToDomain(it)
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

    suspend fun sendDeviceTokenToServer() {
        if (prefs.getBoolean(IS_DEVICE_TOKEN_RETRIEVED, false)) return

        val token = setupCloudMessaging()
        sendDeviceTokenToServer(token)
    }

    private suspend fun sendDeviceTokenToServer(token: String?) {
        try {
            token?.let {
                authService.sendDeviceToken(prefs.getString(ACCESS_TOKEN, null), token)
                prefs.edit().putBoolean(IS_DEVICE_TOKEN_RETRIEVED, true).apply()
            }
        } catch (ex: Exception) {

        }
    }

    private suspend fun setupCloudMessaging() = suspendCancellableCoroutine<String?> { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                cont.resume(null)
            }

            cont.resume(task.result)
        }
    }

    companion object {
        const val LIST_KEY_MESSAGES = "messages"
    }
}