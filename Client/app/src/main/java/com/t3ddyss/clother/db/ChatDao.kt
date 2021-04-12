package com.t3ddyss.clother.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.offers.Offer
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<Chat>)

    // Using last_message_id instead of created_at because I don't have time
    // to write type converter for DateTime data type
    @Query("SELECT * FROM chat ORDER BY last_message_id DESC")
    fun getAllChats(): Flow<Chat>

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()
}