package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Message

interface NotificationInteractor {
    fun initialize()
    fun showMessageNotificationIfNeeded(message: Message)
    fun cancelMessageNotifications(interlocutorId: Int)
}