package com.t3ddyss.clother.models.chat

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.models.user.User

@Entity(tableName = "chat")
data class Chat(@PrimaryKey val id: Int,

                @SerializedName("interlocutor_id")
                @ColumnInfo(name = "interlocutor_id", index = true)
                val interlocutorId: Int
) {
    @Ignore
    lateinit var interlocutor: User

    @SerializedName("last_message")
    @Ignore
    lateinit var lastMessage: Message
}