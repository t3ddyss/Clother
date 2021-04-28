package com.t3ddyss.clother.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.t3ddyss.clother.data.LiveMessagesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnClearFromRecentService @Inject constructor() : Service() {
    @Inject
    lateinit var repository: LiveMessagesRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        repository.disconnectFromServer()
    }
}