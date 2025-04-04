package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkMessage

@JsonClass(generateAdapter = true)
data class VkPinnedMessageData(
    @Json(name = "id") val id: Long?,
    @Json(name = "peer_id") val peerId: Long?,
    @Json(name = "date") val date: Int,
    @Json(name = "from_id") val fromId: Long,
    @Json(name = "out") val out: Boolean?,
    @Json(name = "text") val text: String,
    @Json(name = "conversation_message_id") val conversationMessageId: Long,
    @Json(name = "fwd_messages") val forwards: List<VkMessageData>?,
    @Json(name = "important") val important: Boolean = false,
    @Json(name = "random_id") val randomId: Long = 0,
    @Json(name = "attachments") val attachments: List<VkAttachmentItemData>?,
    @Json(name = "is_hidden") val isHidden: Boolean = false,
    @Json(name = "payload") val payload: String?,
    @Json(name = "geo") val geo: VkMessageData.Geo?,
    @Json(name = "action") val action: VkMessageData.Action?,
    @Json(name = "ttl") val ttl: Int?,
    @Json(name = "reply_message") val replyMessage: VkMessageData?,
    @Json(name = "update_time") val updateTime: Int?
) {

    fun mapToDomain(): VkMessage = VkMessage(
        id = id ?: -1,
        cmId = conversationMessageId,
        text = text.ifBlank { null },
        isOut = out == true,
        peerId = peerId ?: -1,
        fromId = fromId,
        date = date,
        randomId = randomId,
        action = VkMessage.Action.parse(action?.type),
        actionMemberId = action?.memberId,
        actionText = action?.text,
        actionConversationMessageId = action?.conversationMessageId,
        actionMessage = action?.message,
        geoType = geo?.type,
        isImportant = important,
        updateTime = updateTime,
        forwards = forwards.orEmpty().map(VkMessageData::asDomain),

        // TODO: 05/05/2024, Danil Nikolaev: parse attachments
        attachments = emptyList(),
        replyMessage = replyMessage?.asDomain(),
        user = null,
        group = null,
        actionUser = null,
        actionGroup = null,
        pinnedAt = null,
        isPinned = true,
        isSpam = false,
        formatData = null,
    )
}
