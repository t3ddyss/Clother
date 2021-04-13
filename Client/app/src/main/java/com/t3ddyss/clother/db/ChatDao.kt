package com.t3ddyss.clother.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.chat.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<Chat>)

    // Using last_message_id instead of created_at because DateTime stored as String
    // and Strings are slower to compare
    @Query("SELECT * FROM chat ORDER BY last_message_id DESC")
    fun getAllChats(): Flow<Chat>

    @Query("SELECT id FROM chat WHERE interlocutor_id == :interlocutorId LIMIT 1")
    suspend fun getChatIdByInterlocutorId(interlocutorId: Int): Int?

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()
}