package dev.meloda.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.meloda.fast.model.api.domain.VkMessage
import dev.meloda.fast.model.api.domain.VkUnknownAttachment

@Entity(tableName = "messages")
data class VkMessageEntity(
    @PrimaryKey val id: Long,
    val conversationMessageId: Long,
    val text: String?,
    val isOut: Boolean,
    val peerId: Long,
    val fromId: Long,
    val date: Int,
    val randomId: Long,
    val action: String?,
    val actionMemberId: Long?,
    val actionText: String?,
    val actionConversationMessageId: Long?,
    val actionMessage: String?,
    val updateTime: Int?,
    val important: Boolean,
    val forwardIds: List<Long>?,
    val attachments: List<String>?, // TODO: 01/05/2024, Danil Nikolaev: how to store???
    val replyMessageId: Long?,
    val geoType: String?,
    val pinnedAt: Int?,
    val isPinned: Boolean
)

fun VkMessageEntity.asExternalModel(): VkMessage = VkMessage(
    id = id,
    cmId = conversationMessageId,
    text = text,
    isOut = isOut,
    peerId = peerId,
    fromId = fromId,
    date = date,
    randomId = randomId,
    action = VkMessage.Action.parse(action),
    actionMemberId = actionMemberId,
    actionText = actionText,
    actionConversationMessageId = actionConversationMessageId,
    actionMessage = actionMessage,
    updateTime = updateTime,
    isImportant = important,
    forwards = emptyList(),//forwards.orEmpty().map(VkMessageEntity::asExternalModel),
    // TODO: 05/05/2024, Danil Nikolaev: restore attachments
    attachments = attachments.orEmpty().map { VkUnknownAttachment },
    replyMessage = null,//replyMessage?.asExternalModel(),
    geoType = geoType,
    user = null,
    group = null,
    actionUser = null,
    actionGroup = null,
    pinnedAt = pinnedAt,
    isPinned = isPinned,
    isSpam = false,
    formatData = null,
)
