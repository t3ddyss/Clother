package com.t3ddyss.clother.data

import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.db.MessageDao
import com.t3ddyss.clother.db.RemoteKeyDao
import com.t3ddyss.clother.models.common.LoadResult
import com.t3ddyss.clother.models.common.LoadType
import com.t3ddyss.clother.models.user.User
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.lang.Exception
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
//    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var messageLoader: MessagesPagingLoader

    fun getMessages(interlocutor: User) = messageDao
            .getMessagesByInterlocutorId(interlocutor.id)

    suspend fun fetchMessages(interlocutor: User): LoadResult {
        if (!this@MessagesRepository::messageLoader.isInitialized) {
            messageLoader = MessagesPagingLoader(
                    service = service,
                    prefs = prefs,
                    db = db,
                    chatDao = chatDao,
                    messageDao = messageDao,
                    remoteKeyDao = remoteKeyDao,
                    remoteKeyList = KEY_LIST,
                    interlocutor = interlocutor
            )
        }

        return messageLoader.load()
    }

    suspend fun sendDeviceTokenToServer() {
        val token = setupCloudMessaging()
        sendDeviceTokenToServer(token)
    }

    private suspend fun sendDeviceTokenToServer(token: String?) {
        try {
            token?.let {
                authService.sendDeviceToken(prefs.getString(ACCESS_TOKEN, null), token)
            }
        }
        catch (ex: Exception) {

        }
    }

    private suspend fun setupCloudMessaging() = suspendCancellableCoroutine<String?> { cont ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d(DEBUG_TAG, "Fetching FCM registration token failed", task.exception)
                cont.resume(null)
            }

            cont.resume(task.result)
        }
    }

    companion object {
        const val KEY_LIST = "messages"
    }
}