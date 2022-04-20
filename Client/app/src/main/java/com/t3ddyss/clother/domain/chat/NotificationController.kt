package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Message

interface NotificationController {
    fun createNotificationChannels()
    fun showMessageNotification(message: Message)
    fun cancelMessageNotification(userId: Int)
}