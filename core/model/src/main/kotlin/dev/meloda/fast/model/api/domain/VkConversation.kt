package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.PeerType
import dev.meloda.fast.model.database.VkConversationEntity

data class VkConversation(
    val id: Int,
    val localId: Int,
    val ownerId: Int?,
    val title: String?,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val isCallInProgress: Boolean,
    val isPhantom: Boolean,
    val lastConversationMessageId: Int,
    val inReadCmId: Int,
    val outReadCmId: Int,
    val inRead: Int,
    val outRead: Int,
    val lastMessageId: Int?,
    val unreadCount: Int,
    val membersCount: Int?,
    val canChangePin: Boolean,
    val canChangeInfo: Boolean,
    val majorId: Int,
    val minorId: Int,
    val pinnedMessageId: Int?,
    val interactionType: Int,
    val interactionIds: List<Int>,
    val peerType: PeerType,
    val lastMessage: VkMessage?,
    val pinnedMessage: VkMessage?,
    val user: VkUser?,
    val group: VkGroupDomain?
) {

    fun isPinned(): Boolean = majorId > 0
    fun isInUnread() = inRead - (lastMessageId ?: 0) < 0
    fun isOutUnread() = outRead - (lastMessageId ?: 0) < 0
}

fun VkConversation.asEntity(): VkConversationEntity = VkConversationEntity(
    id = id,
    localId = localId,
    ownerId = ownerId,
    title = title,
    photo50 = photo50,
    photo100 = photo100,
    photo200 = photo200,
    isPhantom = isPhantom,
    lastConversationMessageId = lastConversationMessageId,
    inReadCmId = inReadCmId,
    outReadCmId = outReadCmId,
    inRead = inRead,
    outRead = outRead,
    lastMessageId = lastMessageId,
    unreadCount = unreadCount,
    membersCount = membersCount,
    canChangePin = canChangePin,
    canChangeInfo = canChangeInfo,
    majorId = majorId,
    minorId = minorId,
    pinnedMessageId = pinnedMessageId,
    peerType = peerType.value,
)
