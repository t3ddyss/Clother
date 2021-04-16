package com.t3ddyss.clother.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.t3ddyss.clother.api.ClotherAuthService
import com.t3ddyss.clother.data.MessagesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClotherFirebaseMessagingService : FirebaseMessagingService() {
    @Inject lateinit var service: ClotherAuthService

    override fun onNewToken(token: String) {
//        GlobalScope.launch {
//            service.sendDeviceToken(token)
//        }
    }
}