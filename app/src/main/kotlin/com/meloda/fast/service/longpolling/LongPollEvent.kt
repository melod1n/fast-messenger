package com.meloda.fast.service.longpolling

import com.meloda.fast.api.model.InteractionType
import com.meloda.fast.api.model.domain.VkGroupDomain
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.api.model.domain.VkUserDomain

sealed interface LongPollEvent {

    data class VkMessageNewEvent(
        val message: VkMessageDomain,
        val profiles: List<VkUserDomain>,
        val groups: List<VkGroupDomain>,
    ) : LongPollEvent

    data class VkMessageEditEvent(val message: VkMessageDomain) : LongPollEvent

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
