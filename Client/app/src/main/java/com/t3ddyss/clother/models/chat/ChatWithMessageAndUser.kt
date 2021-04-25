package com.t3ddyss.clother.models.chat

import androidx.room.ColumnInfo
import java.util.*

data class ChatWithMessageAndUser(@ColumnInfo(name = "local_chat_id")
                                  val localChatId: Int,

                                  @ColumnInfo(name = "local_message_id")
                                  val localMessageId: Int,

                                  @ColumnInfo(name = "message_user_id")
                                  val messageUserId: Int,

                                  @ColumnInfo(name = "message_created_at")
                                  val messageCreatedAt: Date,

                                  @ColumnInfo(name = "message_body")
                                  val messageBody: String?,

                                  @ColumnInfo(name = "message_image")
                                  val messageImage: String?,

                                  @ColumnInfo(name = "interlocutor_id")
                                  val interlocutorId: Int,

                                  @ColumnInfo(name = "interlocutor_name")
                                  val interlocutorName: String,
)