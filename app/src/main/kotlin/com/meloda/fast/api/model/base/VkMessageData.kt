package com.meloda.fast.api.model.base

import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.VkMessageDomain
import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkMessageData(
    val id: Int,
    val peer_id: Int,
    val date: Int,
    val from_id: Int,
    val out: Int?,
    val text: String,
    val conversation_message_id: Int,
    val fwd_messages: List<VkMessageData>? = emptyList(),
    val important: Boolean = false,
    val random_id: Int = 0,
    val attachments: List<BaseVkAttachmentItem> = emptyList(),
    val is_hidden: Boolean = false,
    val payload: String?,
    val geo: Geo?,
    val action: Action?,
    val ttl: Int?,
    val reply_message: VkMessageData?,
    val update_time: Int?
) {

    fun asVkMessage() = VkMessageDomain(
        id = id,
        text = text.ifBlank { null },
        isOut = out == 1,
        peerId = peer_id,
        fromId = from_id,
        date = date,
        randomId = random_id,
        action = action?.type,
        actionMemberId = action?.member_id,
        actionText = action?.text,
        actionConversationMessageId = action?.conversation_message_id,
        actionMessage = action?.message,
        geo = geo,
        important = important,
        updateTime = update_time,
        forwards = VkUtils.parseForwards(fwd_messages).orEmpty(),
        attachments = VkUtils.parseAttachments(attachments).orEmpty(),
        replyMessage = VkUtils.parseReplyMessage(reply_message)
    )

    @JsonClass(generateAdapter = true)
    data class Geo(
        val type: String,
        val coordinates: Coordinates,
        val place: Place
    ) {

        @JsonClass(generateAdapter = true)
        data class Coordinates(val latitude: Float, val longitude: Float)

        @JsonClass(generateAdapter = true)
        data class Place(val country: String, val city: String, val title: String)
    }

    @JsonClass(generateAdapter = true)
    data class Action(
        val type: String,
        val member_id: Int?,
        val text: String?,
        val conversation_message_id: Int?,
        val message: String?
    )

}
