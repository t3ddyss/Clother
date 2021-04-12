package com.t3ddyss.clother.data

import android.content.SharedPreferences
import com.t3ddyss.clother.api.ClotherChatService
import com.t3ddyss.clother.db.AppDatabase
import com.t3ddyss.clother.db.ChatDao
import com.t3ddyss.clother.models.Error
import com.t3ddyss.clother.models.Failed
import com.t3ddyss.clother.models.ResponseState
import com.t3ddyss.clother.models.Success
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import com.t3ddyss.clother.utilities.handleError
import retrofit2.HttpException
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.inject.Inject

class ChatsRepository @Inject constructor(
    private val service: ClotherChatService,
    private val db: AppDatabase,
    private val chatDao: ChatDao,
    private val prefs: SharedPreferences
) {
    suspend fun getChats(): ResponseState<List<Chat>> {
        return try {
            val chats = service.getChats(prefs.getString(ACCESS_TOKEN, null))
            Success(chats)

        } catch (ex: HttpException) {
            handleError(ex)

        } catch (ex: ConnectException) {
            Failed()

        } catch (ex: SocketTimeoutException) {
            Error(null)
        }
    }
}