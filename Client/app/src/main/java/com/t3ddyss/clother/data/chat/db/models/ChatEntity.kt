package com.t3ddyss.clother.data.chat.db.models

import androidx.room.*
import com.t3ddyss.clother.data.auth.db.models.UserEntity

@Entity(
    tableName = "chat",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("interlocutor_id")
    )],
    indices = [
        Index(value = ["server_id"], unique = true),
        Index(value = ["interlocutor_id"], unique = true)
    ]
)
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    var localId: Int = 0,

    @ColumnInfo(name = "server_id")
    var serverId: Int? = null,

    @ColumnInfo(name = "interlocutor_id")
    val interlocutorId: Int
)