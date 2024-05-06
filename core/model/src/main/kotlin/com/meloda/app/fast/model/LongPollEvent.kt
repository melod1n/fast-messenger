package com.meloda.app.fast.model

import com.meloda.app.fast.model.api.domain.VkMessage

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
}
