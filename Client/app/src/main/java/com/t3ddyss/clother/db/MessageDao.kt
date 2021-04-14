package com.t3ddyss.clother.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.chat.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<Message>)

    @Query("""SELECT message.* FROM chat, message
                    WHERE chat.interlocutor_id == :interlocutorId 
                    AND chat.id == message.chat_id
                    ORDER BY message.created_at DESC""")
    fun getMessagesByInterlocutorId(interlocutorId: Int): Flow<List<Message>>

    @Query("DELETE FROM message WHERE chat_id == :chatId")
    suspend fun deleteAllMessagesFromChat(chatId: Int?)
}