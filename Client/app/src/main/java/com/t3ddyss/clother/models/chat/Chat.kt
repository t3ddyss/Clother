package com.t3ddyss.clother.models.chat

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.t3ddyss.clother.models.user.User

@Entity(tableName = "chat")
data class Chat(@PrimaryKey val id: Int,

                @Embedded(prefix = "interlocutor_")
                val interlocutor: User,

                @Embedded(prefix = "last_message_")
                val lastMessage: Message
)