package com.t3ddyss.clother.models.chat

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "message",
        foreignKeys = [ForeignKey(
                entity = Chat::class,
                parentColumns = arrayOf("server_id"),
                childColumns = arrayOf("server_chat_id"))],
        indices = [Index(value = ["server_id"], unique = true)])
data class Message(@PrimaryKey(autoGenerate = true)
                   @SerializedName("local_id")
                   @ColumnInfo(name = "local_id")
                   var localId: Int = 0,

                   @SerializedName("server_id")
                   @ColumnInfo(name = "server_id")
                   val serverId: Int? = null,

                   @SerializedName("local_chat_id")
                   @ColumnInfo(name = "local_chat_id", index = true)
                   var localChatId: Int,

                   @SerializedName("server_chat_id")
                   @ColumnInfo(name = "server_chat_id", index = true)
                   var serverChatId: Int? = null,

                   @SerializedName("user_id")
                   @ColumnInfo(name = "user_id", index = true)
                   val userId: Int,

                   var status: MessageStatus = MessageStatus.DELIVERED,

                   @SerializedName("created_at")
                   @ColumnInfo(name = "created_at")
                   val createdAt: Date,

                   val body: String?,
                   val image: String?
) {
    @SerializedName("user_name")
    @Ignore
    lateinit var userName: String
}
