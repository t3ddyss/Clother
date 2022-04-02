package com.t3ddyss.clother.data.db

import androidx.room.*
import com.t3ddyss.clother.data.db.entity.ChatEntity
import com.t3ddyss.clother.data.db.entity.ChatWithLastMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: ChatEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(chat: ChatEntity)

    @Transaction
    @Query(
        """SELECT
                    c.local_id AS chat_local_id,
                    c.server_id AS chat_server_id,
                    c.interlocutor_id AS chat_interlocutor_id,
                    c.interlocutor_name AS chat_interlocutor_name,
                    c.interlocutor_email AS chat_interlocutor_email,
                    c.interlocutor_image AS chat_interlocutor_image,
                    m.local_id AS message_local_id,
                    m.server_id AS message_server_id,
                    m.local_chat_id AS message_local_chat_id,
                    m.server_chat_id AS message_server_chat_id,
                    m.user_id AS message_user_id,
                    m.status AS message_status,
                    m.created_at AS message_created_at,
                    m.body AS message_body,
                    m.image AS message_image
                    FROM (SELECT local_id, local_chat_id, MAX(created_at) AS max_created_at
                          FROM message
                          WHERE status != 2
                          GROUP BY local_chat_id) m1
                    INNER JOIN message m 
                    ON m.local_id == m1.local_id AND m.created_at == m1.max_created_at
                    INNER JOIN chat c
                    ON m1.local_chat_id == c.local_id
                    ORDER BY m1.max_created_at DESC
                """
    )
    fun observeChats(): Flow<List<ChatWithLastMessageEntity>>

    @Query("SELECT * FROM chat WHERE local_id == :localId LIMIT 1")
    suspend fun getChatByLocalId(localId: Int): ChatEntity?

    @Query("SELECT * FROM chat WHERE interlocutor_id == :interlocutorId LIMIT 1")
    suspend fun getChatByInterlocutorId(interlocutorId: Int): ChatEntity?

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()

    @Delete
    suspend fun delete(chat: ChatEntity)

    @Query("DELETE FROM chat WHERE server_id NOT IN (:serverIds)")
    suspend fun deleteUncreatedChats(serverIds: Array<Long>)
}