package com.t3ddyss.clother

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.gms.maps.MapsInitializer
import com.t3ddyss.clother.utilities.MESSAGES_CHANNEL_ID
import dagger.hilt.android.HiltAndroidApp
import java.util.*

@HiltAndroidApp
class ClotherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = applicationInfo.loadLabel(packageManager).toString()
            val descriptionText = "Messages"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(MESSAGES_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }


            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}