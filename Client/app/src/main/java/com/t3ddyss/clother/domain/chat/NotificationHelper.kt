package com.t3ddyss.clother.domain.chat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.t3ddyss.clother.R
import com.t3ddyss.clother.domain.chat.models.Message
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

// TODO refactor to interactor & repository
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationId = AtomicInteger(1)
    private val users = mutableMapOf<Int, Boolean>()

    var isChatsFragment = false
    var isChatFragment = false
    var currentInterlocutorId: Int? = null

    private fun shouldDisplayNotification(senderId: Int): Boolean {
        return !(isChatFragment && currentInterlocutorId == senderId || isChatsFragment)
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.messages)
            val descriptionText = context.getString(R.string.messages)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                context.getString(R.string.default_notification_channel_id),
                name,
                importance
            ).apply {
                description = descriptionText
            }

            val notificationManager = context
                .getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotificationIfShould(message: Message) {
        if (!shouldDisplayNotification(message.userId)) return

        val singleNotification = NotificationCompat.Builder(
            context,
            context.getString(R.string.default_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(message.userName)
            .setContentText(message.body ?: context.getString(R.string.image))
            .setGroup(message.userId.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId.getAndIncrement(), singleNotification)
        }

        if (message.userId !in users.keys) {
            users[message.userId] = false
            return
        }

        // TODO retrieve shown notifications and use them to decide
        //  whether new group should be created or not
        if (users[message.userId] == true) {
            return
        }

        val summaryNotification = NotificationCompat.Builder(
            context,
            context.getString(R.string.default_notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_chat)
            .setGroup(message.userId.toString())
            .setGroupSummary(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        with(NotificationManagerCompat.from(context)) {
            users[message.userId] = true
            notify(notificationId.getAndIncrement(), summaryNotification)
        }
    }
}