package com.t3ddyss.clother.data.chat.remote

import arrow.core.Either
import arrow.retrofit.adapter.either.networkhandling.CallError
import com.t3ddyss.clother.data.chat.remote.models.ChatDto
import com.t3ddyss.clother.data.chat.remote.models.MessageDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface RemoteChatService {
    @GET("api/chats")
    suspend fun getChats(@Header("Authorization") accessToken: String?): Either<CallError, List<ChatDto>>

    @GET("api/chats/{interlocutor_id}")
    suspend fun getMessages(
        @Path("interlocutor_id") interlocutorId: Int,
        @Header("Authorization") accessToken: String?,
        @Query("after") afterKey: Int? = null,
        @Query("before") beforeKey: Int? = null,
        @Query("limit") limit: Int
    ): Either<CallError, List<MessageDto>>

    @Multipart
    @POST("api/chats/message?return_chat=false")
    suspend fun sendMessageAndGetIt(
        @Header("Authorization") accessToken: String?,
        @Query("to") interlocutorId: Int,
        @Part("request") body: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): MessageDto

    @Multipart
    @POST("api/chats/message?return_chat=true")
    suspend fun sendMessageAndGetChat(
        @Header("Authorization") accessToken: String?,
        @Query("to") interlocutorId: Int,
        @Part("request") body: RequestBody,
        @Part images: List<MultipartBody.Part>?
    ): ChatDto

    @DELETE("api/chats/message/{message_id}")
    suspend fun deleteMessage(
        @Header("Authorization") accessToken: String?,
        @Path("message_id") messageId: Int
    ): Either<CallError, Unit>
}