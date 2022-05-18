package com.t3ddyss.clother.data.chat

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavDeepLinkBuilder
import com.t3ddyss.clother.R
import com.t3ddyss.clother.domain.chat.NotificationController
import com.t3ddyss.clother.domain.chat.models.Message
import com.t3ddyss.navigation.presentation.models.UserArg
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationController {

    private val notificationManager by lazy {
        context.getSystemService(Application.NOTIFICATION_SERVICE) as NotificationManager
    }
    private val notificationManagerCompat by lazy {
        NotificationManagerCompat.from(context)
    }
    private val notificationId = AtomicInteger()

    override fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MESSAGES_CHANNEL_ID,
                context.getString(R.string.notification_channel_messages_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_messages_description)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun showMessageNotification(message: Message) {
        val userId = message.userId
        val args = bundleOf("user" to UserArg(id = userId, name = message.userName))
        val pendingIntent = NavDeepLinkBuilder(context)
            .setGraph(R.navigation.main_graph)
            .addDestination(R.id.chatsFragment, null)
            .addDestination(R.id.chatFragment, args)
            .createPendingIntent()

        val tag = NotificationTag.MESSAGE.toTag(userId)
        val singleNotification = NotificationCompat.Builder(
            context,
            context.getString(R.string.notification_channel_messages_name)
        )
            .setSmallIcon(R.drawable.ic_chat)
            .setContentTitle(message.userName)
            .setContentText(message.body ?: context.getString(R.string.chat_image_icon))
            .setGroup(userId.toString())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManagerCompat.apply {
            val notificationsByUserCount =
                notificationManager.activeNotifications.count { it.tag == tag }
            notify(tag, notificationId.getAndIncrement(), singleNotification)

            if (notificationsByUserCount == 1) {
                val summaryNotification = NotificationCompat.Builder(
                    context,
                    context.getString(R.string.notification_channel_messages_name)
                )
                    .setContentTitle(message.userName)
                    .setSmallIcon(R.drawable.ic_chat)
                    .setGroup(message.userId.toString())
                    .setGroupSummary(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .build()

                notify(tag, notificationId.getAndIncrement(), summaryNotification)
            }
        }
    }

    override fun cancelMessageNotification(userId: Int) {
        val tag = NotificationTag.MESSAGE.toTag(userId)
        notificationManager.activeNotifications
            .filter {
                it.tag == tag
            }.forEach {
                notificationManagerCompat.cancel(it.tag, it.id)
            }
    }

    private enum class NotificationTag(val key: String) {
        MESSAGE("message");
        fun toTag(userId: Int) = "${this.key}_$userId"
    }

    private companion object {
        const val MESSAGES_CHANNEL_ID = "Messages"
    }
}