package dev.meloda.fast.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.meloda.fast.common.AppConstants
import dev.meloda.fast.ui.R

object NotificationsUtils {

    @SuppressLint("MissingPermission")
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
        channelId: String = AppConstants.NOTIFICATION_CHANNEL_UNCATEGORIZED,
        priority: NotificationPriority = NotificationPriority.Default,
        contentIntent: PendingIntent? = null,
        category: String? = null,
        actions: List<NotificationCompat.Action> = emptyList(),
    ): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_fast_logo)
            .setContentTitle(title)
            .setPriority(priority.value)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setShowWhen(showWhen)
            .setOngoing(notRemovable)

        if (category != null) {
            builder.setCategory(category)
        }

        if (contentText != null) {
            builder.setContentText(contentText)
        }

        if (bigText != null) {
            builder.setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
        }

        if (timeStampWhen != null) {
            builder.setWhen(timeStampWhen)
        }

        actions.forEach(builder::addAction)

        if (notify) {
            try {
                with(NotificationManagerCompat.from(context)) {
                    notify(customNotificationId ?: -1, builder.build())
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return builder
    }

    enum class NotificationPriority(val value: Int) {
        Default(0), Low(-1), Min(-2), High(1), Max(2)
    }
}
