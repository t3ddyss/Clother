package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Message

interface NotificationInteractor {
    fun showMessageNotificationIfNeeded(message: Message)
}