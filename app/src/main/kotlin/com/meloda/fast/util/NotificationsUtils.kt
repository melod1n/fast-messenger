package com.meloda.fast.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.meloda.fast.R
import com.meloda.fast.screens.main.MainActivity

object NotificationsUtils {

    fun showSimpleNotification(
        context: Context,
        title: String?,
        text: String?,
        customNotificationId: Int? = null,
        showWhen: Boolean = false,
        timeStampWhen: Long? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)


        var builder = NotificationCompat.Builder(context, "simple_notifications")
            .setSmallIcon(R.drawable.ic_fast_logo)
            .setContentTitle(title)
//            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setShowWhen(showWhen)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val KEY_TEXT_REPLY = "key_text_reply"
        val replyLabel = "Reply"
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }

        val action: NotificationCompat.Action =
            NotificationCompat.Action.Builder(
                R.drawable.ic_round_arrow_back_24,
                "Reply", null
            )
                .addRemoteInput(remoteInput)
                .build()

        if (timeStampWhen != null) {
            builder = builder.setWhen(timeStampWhen)
        }

//        builder = builder.addAction(action)


        with(NotificationManagerCompat.from(context)) {
            notify(customNotificationId ?: -1, builder.build())
        }
    }

}