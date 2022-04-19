package com.t3ddyss.clother.data.chat.remote

import com.t3ddyss.clother.data.chat.remote.models.ChatDto
import com.t3ddyss.clother.data.chat.remote.models.MessageDto
import retrofit2.http.*

interface RemoteChatService {
    @GET("api/chats")
    suspend fun getChats(@Header("Authorization") accessToken: String?): List<ChatDto>

    @GET("api/chats/{interlocutor_id}")
    suspend fun getMessages(
        @Path("interlocutor_id") interlocutorId: Int,
        @Header("Authorization") accessToken: String?,
        @Query("after") afterKey: Int? = null,
        @Query("before") beforeKey: Int? = null,
        @Query("limit") limit: Int = 10
    ): List<MessageDto>

    @POST("api/chats/message?return_chat=false")
    suspend fun sendMessageAndGetIt(
        @Header("Authorization") accessToken: String?,
        @Query("to") interlocutorId: Int,
        @Body messageJson: String
    ): MessageDto

    @POST("api/chats/message?return_chat=true")
    suspend fun sendMessageAndGetChat(
        @Header("Authorization") accessToken: String?,
        @Query("to") interlocutorId: Int,
        @Body messageJson: String
    ): ChatDto
}