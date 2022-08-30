package com.meloda.fast.util

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.meloda.fast.R

object NotificationsUtils {

    fun createNotification(
        context: Context,
        title: String? = null,
        contentText: String? = null,
        bigText: String? = null,
        customNotificationId: Int? = null,
        showWhen: Boolean = false,
        timeStampWhen: Long? = null,
        notify: Boolean = false,
        notRemovable: Boolean = false,
        channelId: String = "simple_notifications",
        priority: NotificationPriority = NotificationPriority.Default,
        contentIntent: PendingIntent? = null,
        category: String? = null
    ): NotificationCompat.Builder {
        var builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_fast_logo)
            .setContentTitle(title)
            .setPriority(priority.value)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setShowWhen(showWhen)
            .setOngoing(notRemovable)

        if (category != null) {
            builder = builder.setCategory(category)
        }

        if (contentText != null) {
            builder = builder.setContentText(contentText)
        }

        if (bigText != null) {
            builder = builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }

        if (timeStampWhen != null) {
            builder = builder.setWhen(timeStampWhen)
        }

        if (notify) {
            with(NotificationManagerCompat.from(context)) {
                notify(customNotificationId ?: -1, builder.build())
            }
        }

        return builder
    }

    enum class NotificationPriority(val value: Int) {
        Default(0), Low(-1), Min(-2), High(1), Max(2)
    }

}