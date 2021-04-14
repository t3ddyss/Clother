package com.t3ddyss.clother.models.chat

import androidx.room.*
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "message",
        foreignKeys = [ForeignKey(
                entity = Chat::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("chat_id"))])
data class Message(@PrimaryKey(autoGenerate = true)
                   @SerializedName("local_id")
                   var id: Int = 0,

                   val server_id: Int? = null,

                   @SerializedName("chat_id")
                   @ColumnInfo(name = "chat_id", index = true)
                   val chatId: Int,

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
