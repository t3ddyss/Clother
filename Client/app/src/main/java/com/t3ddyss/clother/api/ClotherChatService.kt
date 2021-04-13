package com.t3ddyss.clother.api

import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.chat.Message
import retrofit2.http.*

interface ClotherChatService {
    @GET("api/chats")
    suspend fun getChats(@Header("Authorization") accessToken: String?): List<Chat>

    @GET("api/chats/{interlocutor_id}")
    suspend fun getMessages(@Path("interlocutor_id") interlocutorId: Int,
                            @Header("Authorization") accessToken: String?,
                            @Query("after") afterKey: Int? = null,
                            @Query("before") beforeKey: Int? = null,
                            @Query("limit") limit: Int = 10
    ) : List<Message>
}