package com.t3ddyss.clother.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.chat.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<Message>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message): Long

    @Query("""SELECT message.* FROM chat, message
                    WHERE chat.interlocutor_id == :interlocutorId 
                    AND (message.local_chat_id == chat.local_id 
                    OR message.server_chat_id == chat.server_id)
                    ORDER BY message.created_at DESC""")
    fun getMessagesByInterlocutorId(interlocutorId: Int): Flow<List<Message>>

    @Query("DELETE FROM message WHERE server_chat_id == :serverChatId")
    suspend fun deleteAllMessagesFromChat(serverChatId: Int?)
}