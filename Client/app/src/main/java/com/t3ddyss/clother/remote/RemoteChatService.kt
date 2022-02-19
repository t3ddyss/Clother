package com.t3ddyss.clother.remote

import com.t3ddyss.clother.models.dto.ChatDto
import com.t3ddyss.clother.models.dto.MessageDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

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
}