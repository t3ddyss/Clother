package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Message
import javax.inject.Inject

class NotificationInteractorImpl @Inject constructor(
    private val notificationHelper: NotificationHelper
) : NotificationInteractor {
    override fun showMessageNotificationIfNeeded(message: Message) {
    }
}