package com.t3ddyss.clother.db

import androidx.room.*
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.user.User
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<Chat>)

    @Update
    suspend fun update(chat: Chat?): Int

    @Query("SELECT * FROM chat ORDER BY last_message_created_at DESC")
    fun getAllChats(): Flow<List<Chat>>

    @Query("SELECT * FROM chat WHERE interlocutor_id == :interlocutorId LIMIT 1")
    suspend fun getChatByInterlocutorId(interlocutorId: Int): Chat?

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()
}