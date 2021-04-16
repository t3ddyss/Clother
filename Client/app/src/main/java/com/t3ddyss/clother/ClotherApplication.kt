package com.t3ddyss.clother

import android.app.Application
import com.t3ddyss.clother.utilities.NotificationUtil
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ClotherApplication : Application() {
    @Inject lateinit var notificationUtil: NotificationUtil

    override fun onCreate() {
        super.onCreate()
        notificationUtil.createNotificationChannel()
    }
}