package com.t3ddyss.clother.data.chat.db.models

import androidx.room.*
import com.t3ddyss.clother.data.auth.db.models.UserEntity

@Entity(tableName = "chat", indices = [Index(value = ["server_id"], unique = true)])
@SuppressWarnings(RoomWarnings.PRIMARY_KEY_FROM_EMBEDDED_IS_DROPPED)
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "local_id")
    var localId: Int = 0,

    @ColumnInfo(name = "server_id")
    var serverId: Int? = null,

    @Embedded(prefix = "interlocutor_")
    val interlocutor: UserEntity
)