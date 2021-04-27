package com.t3ddyss.clother.models.entity

import androidx.room.*

@Entity(tableName = "chat", indices = [Index(value = ["server_id"], unique = true)])
data class ChatEntity(@PrimaryKey(autoGenerate = true)
                      @ColumnInfo(name = "local_id")
                      var localId: Int = 0,

                      @ColumnInfo(name = "server_id")
                      var serverId: Int? = null,

                      @Embedded(prefix = "interlocutor_")
                      val interlocutor: UserEntity)