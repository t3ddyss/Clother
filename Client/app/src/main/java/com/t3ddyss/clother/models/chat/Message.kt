package com.t3ddyss.clother.models.chat

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "message",
        foreignKeys = [ForeignKey(
                entity = Chat::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("chat_id"))])
data class Message(@PrimaryKey val id: Int,

                   @SerializedName("chat_id")
                   @ColumnInfo(name = "chat_id", index = true)
                   val chatId: Int,

                   @SerializedName("user_id")
                   @ColumnInfo(name = "user_id", index = true)
                   val userId: Int,

                   @SerializedName("user_name")
                   @ColumnInfo(name = "user_name")
                   val userName: String,

                   @SerializedName("created_at")
                   @ColumnInfo(name = "created_at")
                   val createdAt: String,

                   val body: String?,
                   val image: String?
)
