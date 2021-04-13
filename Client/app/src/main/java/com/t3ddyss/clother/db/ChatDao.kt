package com.t3ddyss.clother.db

import androidx.room.*
import com.t3ddyss.clother.models.chat.Chat
import com.t3ddyss.clother.models.chat.Message
import com.t3ddyss.clother.models.user.User
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllChats(chats: List<Chat>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInterlocutors(interlocutors: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMessages(messages: List<Message>)

    @Query("""
        SELECT lastMessage.*, interlocutor_id, chat_id
        FROM message lastMessage
        INNER JOIN (SELECT MAX(message.id) AS last_message_id
                    FROM message
                    GROUP BY message.chat_id)
        ON lastMessage.id == last_message_id
        INNER JOIN user last_message_sender
        ON lastMessage.user_id == last_message_sender.id
        INNER JOIN chat c
        ON lastMessage.chat_id = c.id
        INNER JOIN user interlocutor
        ON c.interlocutor_id == interlocutor_id
        ORDER BY lastMessage.id DESC
    """)
    fun getAllChats(): Flow<List<Chat>>

    @Query("SELECT id FROM chat WHERE interlocutor_id == :interlocutorId LIMIT 1")
    suspend fun getChatIdByInterlocutorId(interlocutorId: Int): Int?

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()

    @Transaction
    suspend fun insertChats(chats: List<Chat>) {
        insertAllChats(chats)
        insertAllInterlocutors(chats.map { it.interlocutor })
        insertAllMessages(chats.map { it.lastMessage })
    }
}