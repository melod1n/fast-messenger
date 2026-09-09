package dev.meloda.fast.domain.notifier

import dev.meloda.fast.model.api.domain.VkMessage

interface MessageNotifier {
    fun notifyNewMessage(message: VkMessage)
    fun cancelNotification(peerId: Long)
}
