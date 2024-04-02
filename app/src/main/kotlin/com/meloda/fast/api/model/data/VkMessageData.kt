package com.meloda.fast.api.model.data

import com.meloda.fast.api.VkUtils
import com.meloda.fast.api.model.domain.VkMessageDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkMessageData(
    @Json(name = "id") val id: Int?,
    @Json(name = "peer_id") val peerId: Int?,
    @Json(name = "date") val date: Int,
    @Json(name = "from_id") val fromId: Int,
    @Json(name = "out") val out: Int?,
    @Json(name = "text") val text: String,
    @Json(name = "conversation_message_id") val conversationMessageId: Int,
    @Json(name = "fwd_messages") val fwdMessages: List<VkMessageData>? = emptyList(),
    @Json(name = "important") val important: Boolean = false,
    @Json(name = "random_id") val randomId: Int = 0,
    @Json(name = "attachments") val attachments: List<VkAttachmentItemData> = emptyList(),
    @Json(name = "is_hidden") val isHidden: Boolean = false,
    @Json(name = "payload") val payload: String?,
    @Json(name = "geo") val geo: Geo?,
    @Json(name = "action") val action: Action?,
    @Json(name = "ttl") val ttl: Int?,
    @Json(name = "reply_message") val replyMessage: VkMessageData?,
    @Json(name = "update_time") val updateTime: Int?
) {

    @JsonClass(generateAdapter = true)
    data class Geo(
        @Json(name = "type") val type: String,
        @Json(name = "coordinates") val coordinates: Coordinates,
        @Json(name = "place") val place: Place
    ) {

        @JsonClass(generateAdapter = true)
        data class Coordinates(
            @Json(name = "latitude") val latitude: Float,
            @Json(name = "longitude") val longitude: Float
        )

        @JsonClass(generateAdapter = true)
        data class Place(
            @Json(name = "country") val country: String,
            @Json(name = "city") val city: String,
            @Json(name = "title") val title: String
        )
    }

    @JsonClass(generateAdapter = true)
    data class Action(
        @Json(name = "type") val type: String,
        @Json(name = "member_id") val memberId: Int?,
        @Json(name = "text") val text: String?,
        @Json(name = "conversation_message_id") val conversationMessageId: Int?,
        @Json(name = "message") val message: String?
    )

    fun asVkMessage() = VkMessageDomain(
        id = id ?: -1,
        text = text.ifBlank { null },
        isOut = out == 1,
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
        replyMessage = VkUtils.parseReplyMessage(replyMessage)
    )
}
