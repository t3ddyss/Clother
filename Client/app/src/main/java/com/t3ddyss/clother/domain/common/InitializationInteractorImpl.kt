package com.t3ddyss.clother.domain.common

import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.chat.ChatInteractor
import com.t3ddyss.clother.domain.chat.NotificationHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitializationInteractorImpl @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val chatInteractor: ChatInteractor,
    private val mapRepository: MapRepository,
    private val notificationHelper: NotificationHelper
) : InitializationInteractor {
    override fun initialize() {
        authInteractor.initialize()
        chatInteractor.initialize()
        notificationHelper.createNotificationChannel()
        mapRepository.initialize()
    }
}