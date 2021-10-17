package com.t3ddyss.clother.di

import android.content.Context
import com.google.android.gms.maps.MapView
import com.t3ddyss.clother.data.LiveMessagingRepository
import com.t3ddyss.clother.utilities.NotificationHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Initializer @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val notificationHelper: NotificationHelper,
    private val liveMessagingRepository: LiveMessagingRepository
) {
    fun initialize() {
        notificationHelper.createNotificationChannel()
        liveMessagingRepository.initialize()

        MainScope().launch {
            preloadGoogleMap()
        }
    }

    private suspend fun preloadGoogleMap() = withContext(
        CoroutineExceptionHandler { _, _ -> }
    ) {
        MapView(context).apply {
            onCreate(null)
            onPause()
            onDestroy()
        }
    }
}