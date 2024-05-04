package com.meloda.fast.api.model.data

import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.meloda.fast.ext.isTrue
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkPinnedMessageData(
    @Json(name = "id") val id: Int?,
    @Json(name = "peer_id") val peerId: Int?,
    @Json(name = "date") val date: Int,
    @Json(name = "from_id") val fromId: Int,
    @Json(name = "out") val out: Boolean?,
    @Json(name = "text") val text: String,
    @Json(name = "conversation_message_id") val conversationMessageId: Int,
    @Json(name = "fwd_messages") val fwdMessages: List<VkMessageData>? = emptyList(),
    @Json(name = "important") val important: Boolean = false,
    @Json(name = "random_id") val randomId: Int = 0,
    @Json(name = "attachments") val attachments: List<VkAttachmentItemData> = emptyList(),
    @Json(name = "is_hidden") val isHidden: Boolean = false,
    @Json(name = "payload") val payload: String?,
    @Json(name = "geo") val geo: VkMessageData.Geo?,
    @Json(name = "action") val action: VkMessageData.Action?,
    @Json(name = "ttl") val ttl: Int?,
    @Json(name = "reply_message") val replyMessage: VkMessageData?,
    @Json(name = "update_time") val updateTime: Int?
) {

    fun mapToDomain(): VkMessageDomain = VkMessageDomain(
        id = id ?: -1,
        text = text.ifBlank { null },
        isOut = out.isTrue,
        peerId = peerId ?: -1,
        fromId = fromId,
        date = date,
        randomId = randomId,
        action = action?.type,
        actionMemberId = action?.memberId,
        actionText = action?.text,
        actionConversationMessageId = action?.conversationMessageId,
        actionMessage = action?.message,
        geo = geo,
        important = important,
        updateTime = updateTime,
        forwards = VkUtils.parseForwards(fwdMessages).orEmpty(),
        attachments = VkUtils.parseAttachments(attachments).orEmpty(),

        replyMessage = VkUtils.parseReplyMessage(replyMessage),

        user = null,
        group = null,
        actionUser = null,
        actionGroup = null
    )
}

