package com.t3ddyss.clother.models.chat

import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.models.user.User

@Entity(tableName = "chat")
data class Chat(@PrimaryKey val id: Int,

                @Embedded(prefix = "interlocutor_")
                val interlocutor: User,

                @SerializedName("last_message")
                @Embedded(prefix = "last_message_")
                val lastMessage: Message
)