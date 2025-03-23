package dev.meloda.fast.model

import dev.meloda.fast.model.api.domain.VkMessage

sealed interface LongPollEvent {

    data class VkMessageNewEvent(val message: VkMessage) : LongPollEvent

    data class VkMessageEditEvent(val message: VkMessage) : LongPollEvent

    data class VkMessageReadIncomingEvent(
        val peerId: Int,
        val messageId: Int,
        val unreadCount: Int,
    ) : LongPollEvent

    data class VkMessageReadOutgoingEvent(
        val peerId: Int,
        val messageId: Int,
        val unreadCount: Int,
    ) : LongPollEvent

    data class VkConversationPinStateChangedEvent(
        val peerId: Int,
        val majorId: Int,
    ) : LongPollEvent

    data class Interaction(
        val interactionType: InteractionType,
        val peerId: Int,
        val userIds: List<Int>,
        val totalCount: Int,
        val timestamp: Int
    ) : LongPollEvent

    data class UnreadCounter(
        val unread: Int,
        val unreadUnmuted: Int,
        val showOnlyMuted: Boolean,
        val business: Int,
        val archive: Int,
        val archiveUnmuted: Int,
        val archiveMentions: Int
    ): LongPollEvent
}
