package com.t3ddyss.clother.models.chat

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.models.user.User

@Entity(tableName = "message",
        foreignKeys = [ForeignKey(
                entity = Chat::class,
                parentColumns = arrayOf("id"),
                childColumns = arrayOf("chat_id")),
            ForeignKey(
                    entity = User::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("user_id"))])
data class Message(@PrimaryKey val id: Int,

                   @SerializedName("chat_id")
                   @ColumnInfo(name = "chat_id", index = true)
                   val chatId: Int,

                   @SerializedName("user_id")
                   @ColumnInfo(name = "user_id", index = true) val userId: Int,


                   @SerializedName("created_at")
                   @ColumnInfo(name = "created_at") val createdAt: String,

                   val body: String?,
                   val image: String?
) {
    @SerializedName("user_name")
    lateinit var userName: String
}
