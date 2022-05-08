package com.t3ddyss.clother.data.chat.db

import androidx.room.*
import com.t3ddyss.clother.data.chat.db.models.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(message: MessageEntity)

    @Query(
        """SELECT message.* FROM chat, message
                    WHERE chat.interlocutor_id == :interlocutorId 
                    AND (message.local_chat_id == chat.local_id 
                    OR message.server_chat_id == chat.server_id)
                    ORDER BY message.created_at DESC"""
    )
    fun observeMessagesByInterlocutorId(interlocutorId: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM message WHERE local_id == :localId LIMIT 1")
    suspend fun getMessageByLocalId(localId: Int): MessageEntity

    @Query("DELETE FROM message WHERE server_chat_id == :serverChatId")
    suspend fun deleteAllMessagesFromChat(serverChatId: Int?)

    @Query("DELETE FROM message WHERE server_id IS NULL")
    suspend fun deleteUnsentMessages()

    @Delete
    suspend fun delete(message: MessageEntity)

    @Query("DELETE FROM message WHERE local_id == :localId")
    suspend fun deleteByLocalId(localId: Int)
}