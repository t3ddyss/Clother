package com.t3ddyss.clother

import android.app.Application
import com.google.android.gms.maps.MapView
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

        scope.launch { preloadGoogleMap() }
        notificationUtil.createNotificationChannel()
    }

    private suspend fun preloadGoogleMap() = withContext(Dispatchers.Default) {
        try {
            val mapView = MapView(applicationContext)
            mapView.onCreate(null)
            mapView.onPause()
            mapView.onDestroy()
        } catch (ex: Exception) {

        }
    }
}