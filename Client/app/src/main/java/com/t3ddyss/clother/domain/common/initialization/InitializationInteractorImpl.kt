package com.t3ddyss.clother.domain.common.initialization

import com.t3ddyss.clother.domain.auth.AuthInteractor
import com.t3ddyss.clother.domain.chat.ChatInteractor
import com.t3ddyss.clother.domain.chat.NotificationInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitializationInteractorImpl @Inject constructor(
    private val authInteractor: AuthInteractor,
    private val chatInteractor: ChatInteractor,
    private val notificationInteractor: NotificationInteractor,
    private val mapRepository: MapRepository,
) : InitializationInteractor {
    override fun initialize() {
        authInteractor.initialize()
        chatInteractor.initialize()
        notificationInteractor.initialize()
        mapRepository.initialize()
    }
}