package com.t3ddyss.clother.data.chat.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.t3ddyss.clother.data.auth.db.models.UserEntity
import com.t3ddyss.clother.domain.chat.models.MessageStatus
import java.util.Date

@Entity(
    tableName = "message",
    foreignKeys = [
        ForeignKey(
        entity = ChatEntity::class,
        parentColumns = arrayOf("server_id"),
        childColumns = arrayOf("server_chat_id")
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("user_id")
    )],
    indices = [Index(value = ["server_id"], unique = true)]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    var localId: Int = 0,

    @ColumnInfo(name = "server_id")
    var serverId: Int? = null,

    @ColumnInfo(name = "local_chat_id", index = true)
    var localChatId: Int = 0,

    @ColumnInfo(name = "server_chat_id", index = true)
    var serverChatId: Int? = null,

    @ColumnInfo(name = "user_id", index = true)
    val userId: Int,

    var status: MessageStatus = MessageStatus.DELIVERED,

    @ColumnInfo(name = "created_at")
    var createdAt: Date,

    val body: String?,
    val image: String?
)
