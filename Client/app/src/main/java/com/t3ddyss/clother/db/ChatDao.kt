package com.t3ddyss.clother.db

import androidx.room.*
import com.t3ddyss.clother.models.chat.Chat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<Chat>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: Chat): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(chat: Chat)

    @Query("SELECT * FROM chat ORDER BY last_message_created_at DESC")
    fun getAllChats(): Flow<List<Chat>>

    @Query("SELECT * FROM chat WHERE local_id == :localId LIMIT 1")
    suspend fun getChatByLocalId(localId: Int): Chat?

    @Query("SELECT * FROM chat WHERE interlocutor_id == :interlocutorId LIMIT 1")
    suspend fun getChatByInterlocutorId(interlocutorId: Int): Chat?

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()

    @Delete
    suspend fun delete(chat: Chat)

    @Query("DELETE FROM chat WHERE server_id NOT IN (:serverIds)")
    suspend fun deleteRemovedChats(serverIds: Array<Long>)
}