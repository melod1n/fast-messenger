package com.meloda.app.fast.model.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meloda.app.fast.model.api.domain.VkAttachment
import com.meloda.app.fast.model.api.domain.VkMessage

@Entity(tableName = "messages")
data class VkMessageEntity(
    @PrimaryKey val id: Int,
    val conversationMessageId: Int,
    val text: String?,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int,
    val randomId: Int,
    val action: String?,
    val actionMemberId: Int?,
    val actionText: String?,
    val actionConversationMessageId: Int?,
    val actionMessage: String?,
    val updateTime: Int?,
    val important: Boolean,
    val forwardIds: List<Int>?,
    val attachments: List<String>?, // TODO: 01/05/2024, Danil Nikolaev: how to store???
    val replyMessageId: Int?,
    val geoType: String?
)

fun VkMessageEntity.asExternalModel(): VkMessage = VkMessage(
    id = id,
    conversationMessageId = conversationMessageId,
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
    important = important,
    forwards = emptyList(),//forwards.orEmpty().map(VkMessageEntity::asExternalModel),
    // TODO: 05/05/2024, Danil Nikolaev: restore attachments
    attachments = attachments.orEmpty().map { object : VkAttachment {} },
    replyMessage = null,//replyMessage?.asExternalModel(),
    geoType = geoType,
    user = null,
    group = null,
    actionUser = null,
    actionGroup = null,
)
