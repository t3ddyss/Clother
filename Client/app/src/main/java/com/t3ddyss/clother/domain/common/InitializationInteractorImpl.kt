package com.t3ddyss.clother.domain.common

import com.t3ddyss.clother.data.chat.LiveMessagingRepository
import com.t3ddyss.clother.domain.chat.NotificationHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitializationInteractorImpl @Inject constructor(
    private val mapRepository: MapRepository,
    private val notificationHelper: NotificationHelper,
    private val liveMessagingRepository: LiveMessagingRepository
) : InitializationInteractor {
    override fun initialize() {
        MainScope().launch {
            notificationHelper.createNotificationChannel()
            liveMessagingRepository.initialize()
            mapRepository.initialize()
        }
    }
}