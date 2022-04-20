package com.t3ddyss.clother.domain.chat

import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.clother.domain.common.navigation.NavigationInteractor
import com.t3ddyss.clother.domain.common.navigation.Screen
import javax.inject.Inject

class NotificationInteractorImpl @Inject constructor(
    private val navigationInteractor: NavigationInteractor,
    private val notificationController: NotificationController
) : NotificationInteractor {

    override fun initialize() {
        notificationController.createNotificationChannels()
    }

    override fun showMessageNotificationIfNeeded(message: Message) {
        val isChatsFragment = navigationInteractor.isScreen(Screen.CHATS)
        val isChatWithUser = navigationInteractor.isScreen(Screen.CHAT)
            && navigationInteractor.interlocutorId == message.userId
        if (!isChatsFragment && !isChatWithUser) {
            notificationController.showMessageNotification(message)
        }
    }

    override fun cancelMessageNotifications(interlocutorId: Int) {
        notificationController.cancelMessageNotification(interlocutorId)
    }
}