package dev.meloda.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.meloda.fast.model.api.PeerType
import dev.meloda.fast.model.api.domain.VkConversation

@Entity(tableName = "conversations")
data class VkConversationEntity(
    @PrimaryKey val id: Long,
    val localId: Long,
    val ownerId: Long?,
    val title: String?,
    val photo50: String?,
    val photo100: String?,
    val photo200: String?,
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
    val peerType: String,
)

fun VkConversationEntity.asExternalModel(): VkConversation = VkConversation(
    id = id,
    localId = localId,
    ownerId = ownerId,
    title = title,
    photo50 = photo50,
    photo100 = photo100,
    photo200 = photo200,
    isCallInProgress = false,
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
    interactionType = -1,
    interactionIds = emptyList(),
    peerType = PeerType.parse(peerType),
    lastMessage = null,//lastMessage?.asExternalModel(),
    pinnedMessage = null,//pinnedMessage?.asExternalModel(),
    user = null,
    group = null,
)
