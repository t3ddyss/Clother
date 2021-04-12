package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.chat.Chat
import retrofit2.http.*

interface ClotherChatService {
    @GET("api/chats")
    suspend fun getChats(@Header("Authorization") accessToken: String?): List<Chat>
}