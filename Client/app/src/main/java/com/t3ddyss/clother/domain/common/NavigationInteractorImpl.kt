package com.t3ddyss.clother.domain.common

import com.t3ddyss.clother.domain.chat.NotificationHelper
import javax.inject.Inject

class NavigationInteractorImpl @Inject constructor(
    private val notificationHelper: NotificationHelper
) : NavigationInteractor {
    override var currentInterlocutorId: Int?
        get() = notificationHelper.currentInterlocutorId
        set(value) {
            notificationHelper.currentInterlocutorId = value
        }
}