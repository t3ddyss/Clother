package com.t3ddyss.clother.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.t3ddyss.clother.data.MessagesRepository
import com.t3ddyss.clother.utilities.DEBUG_TAG
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class OnClearFromRecentService @Inject constructor(): Service() {
    @Inject lateinit var repository: MessagesRepository

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        Log.d(DEBUG_TAG, "onTaskRemoved")
        repository.disconnectFromServer()
    }
}