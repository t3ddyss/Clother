package com.t3ddyss.clother.models.chat

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.models.user.User

@Entity(tableName = "chat")
data class Chat(@PrimaryKey val id: Int,

                @Embedded(prefix = "interlocutor_")
                val interlocutor: User,

                @Embedded(prefix = "last_message_")
                @SerializedName("last_message")
                val lastMessage: Message
)