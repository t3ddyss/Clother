package com.t3ddyss.clother

import android.app.Application
import android.content.Intent
import com.google.android.gms.maps.MapView
import com.t3ddyss.clother.services.OnClearFromRecentService
import com.t3ddyss.clother.utilities.NotificationUtil
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltAndroidApp
class ClotherApplication : Application() {
    private val scope = MainScope()
    @Inject
    lateinit var notificationUtil: NotificationUtil

    override fun onCreate() {
        super.onCreate()

        scope.launch { setupGoogleMap() }
        notificationUtil.createNotificationChannel()
        startService(Intent(applicationContext, OnClearFromRecentService::class.java))
    }

    private suspend fun setupGoogleMap() = withContext(Dispatchers.Default) {
        try {
            val mapView = MapView(applicationContext)
            mapView.onCreate(null)
            mapView.onPause()
            mapView.onDestroy()
        } catch (ex: Exception) {

        }
    }
}