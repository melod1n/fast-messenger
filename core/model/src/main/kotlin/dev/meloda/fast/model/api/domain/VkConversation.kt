package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.PeerType
import dev.meloda.fast.model.database.VkConversationEntity

data class VkConversation(
    val id: Long,
    val localId: Long,
    val ownerId: Long?,
    val title: String?,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
    val isCallInProgress: Boolean,
    val isPhantom: Boolean,
    val lastConversationMessageId: Long,
    val inReadCmId: Long,
    val outReadCmId: Long,
    val inRead: Long,
    val outRead: Long,
    val lastMessageId: Long?,
    val unreadCount: Int,
    val membersCount: Int?,
    val canChangePin: Boolean,
    val canChangeInfo: Boolean,
    val majorId: Int,
    val minorId: Int,
    val pinnedMessageId: Long?,
    val interactionType: Int,
    val interactionIds: List<Long>,
    val peerType: PeerType,
    val lastMessage: VkMessage?,
    val pinnedMessage: VkMessage?,
    val user: VkUser?,
    val group: VkGroupDomain?
) {

    fun isPinned(): Boolean = majorId > 0
    fun isInUnread() = inRead - (lastMessageId ?: 0) < 0
    fun isOutUnread() = outRead - (lastMessageId ?: 0) < 0

    companion object {
        val EMPTY: VkConversation = VkConversation(
            id = -1,
            localId = -1,
            ownerId = null,
            title = "...",
            photo50 = null,
            photo100 = null,
            photo200 = null,
            isCallInProgress = false,
            isPhantom = false,
            lastConversationMessageId = -1,
            inReadCmId = -1,
            outReadCmId = -1,
            inRead = -1,
            outRead = -1,
            lastMessageId = null,
            unreadCount = -1,
            membersCount = null,
            canChangePin = false,
            canChangeInfo = false,
            majorId = -1,
            minorId = -1,
            pinnedMessageId = null,
            interactionType = -1,
            interactionIds = emptyList(),
            peerType = PeerType.USER,
            lastMessage = null,
            pinnedMessage = null,
            user = null,
            group = null

        )
    }
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
