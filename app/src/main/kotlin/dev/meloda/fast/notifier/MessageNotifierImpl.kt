package dev.meloda.fast.notifier

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.meloda.fast.common.ActiveChatTracker
import dev.meloda.fast.common.AppConstants
import dev.meloda.fast.data.UserConfig
import dev.meloda.fast.domain.notifier.MessageNotifier
import dev.meloda.fast.model.api.domain.VkAttachment
import dev.meloda.fast.model.api.domain.VkAudioDomain
import dev.meloda.fast.model.api.domain.VkAudioMessageDomain
import dev.meloda.fast.model.api.domain.VkFileDomain
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkPhotoDomain
import dev.meloda.fast.model.api.domain.VkStickerDomain
import dev.meloda.fast.model.api.domain.VkVideoDomain
import dev.meloda.fast.model.api.domain.VkVideoMessageDomain
import dev.meloda.fast.presentation.MainActivity
import dev.meloda.fast.ui.R
import dev.meloda.fast.util.NotificationsUtils

class MessageNotifierImpl(
    private val context: Context
) : MessageNotifier {

    override fun notifyNewMessage(message: VkMessage) {
        if (message.isOut || message.fromId == UserConfig.userId) return
        if (ActiveChatTracker.isActiveForegroundChat(message.peerId)) return

        val user = message.user
        val group = message.group
        val actionUser = message.actionUser

        val title = when {
            user != null -> "${user.firstName} ${user.lastName}".trim()
            group != null -> group.name
            actionUser != null -> "${actionUser.firstName} ${actionUser.lastName}".trim()
            else -> context.getString(R.string.notification_new_message)
        }

        val messageText = message.text
        val attachments = message.attachments
        val actionText = message.actionText

        val text = when {
            !messageText.isNullOrBlank() -> messageText
            !attachments.isNullOrEmpty() -> formatAttachment(attachments.first())
            !actionText.isNullOrBlank() -> actionText
            else -> context.getString(R.string.notification_new_message)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_PEER_ID, message.peerId)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            message.peerId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        NotificationsUtils.createNotification(
            context = context,
            title = title,
            contentText = text,
            bigText = text,
            customNotificationId = message.peerId.toInt(),
            showWhen = true,
            timeStampWhen = (message.date.toLong() * 1000L).takeIf { it > 0 }
                ?: System.currentTimeMillis(),
            notify = true,
            channelId = AppConstants.NOTIFICATION_CHANNEL_MESSAGES,
            priority = NotificationsUtils.NotificationPriority.High,
            contentIntent = pendingIntent,
            category = NotificationCompat.CATEGORY_MESSAGE
        )
    }

    override fun cancelNotification(peerId: Long) {
        val manager = NotificationManagerCompat.from(context)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (!manager.areNotificationsEnabled()) return
        }

        try {
            manager.cancel(peerId.toInt())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatAttachment(attachment: VkAttachment): String = when (attachment) {
        is VkAudioMessageDomain -> context.getString(R.string.notification_voice_message)
        is VkVideoMessageDomain -> context.getString(R.string.notification_video_message)
        is VkPhotoDomain -> context.getString(R.string.notification_photo)
        is VkStickerDomain -> context.getString(R.string.notification_sticker)
        is VkVideoDomain -> context.getString(R.string.notification_video)
        is VkAudioDomain -> context.getString(R.string.notification_audio)
        is VkFileDomain -> context.getString(R.string.notification_file)
        else -> context.getString(R.string.notification_attachment)
    }
}
