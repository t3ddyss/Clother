package com.t3ddyss.clother.services

import android.content.SharedPreferences
import com.google.firebase.messaging.FirebaseMessagingService
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.utilities.ACCESS_TOKEN
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class ClotherFirebaseMessagingService : FirebaseMessagingService() {
    private val scope = MainScope()
    @Inject
    lateinit var service: ClotherAuthService
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