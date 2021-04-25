package com.t3ddyss.clother.models.chat

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.models.user.User

// TODO normalize table (remove last_message) to avoid update anomalies, also use different classes for network and local data
@Entity(tableName = "chat", indices = [Index(value = ["server_id"], unique = true)])
data class Chat(@PrimaryKey(autoGenerate = true)
                @SerializedName("local_id")
                @ColumnInfo(name = "local_id")
                var localId: Int = 0,

                @SerializedName("server_id")
                @ColumnInfo(name = "server_id")
                val serverId: Int? = null,

                @Embedded(prefix = "interlocutor_")
                val interlocutor: User? = null,
) {
    @SerializedName("last_message")
    @Ignore
    var lastMessage: Message? = null
}