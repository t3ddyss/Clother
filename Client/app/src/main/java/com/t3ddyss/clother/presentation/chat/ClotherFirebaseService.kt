package com.t3ddyss.clother.presentation.chat

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.t3ddyss.clother.data.remote.RemoteAuthService
import com.t3ddyss.clother.util.ACCESS_TOKEN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClotherFirebaseService : FirebaseMessagingService() {
    private val scope = MainScope()
    @Inject
    lateinit var service: RemoteAuthService
    @Inject
    lateinit var prefs: SharedPreferences

    override fun onNewToken(token: String) {
        val handler = CoroutineExceptionHandler { _, _ -> }

        scope.launch(handler) {
            service.sendDeviceToken(
                accessToken = prefs.getString(ACCESS_TOKEN, null),
                token = token
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}