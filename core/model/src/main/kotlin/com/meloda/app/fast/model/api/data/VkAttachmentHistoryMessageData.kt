package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkAttachmentHistoryMessage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAttachmentHistoryMessageData(
    @Json(name = "message_id") val messageId: Int,
    @Json(name = "date") val date: Int,
    @Json(name = "cmid") val conversationMessageId: Int,
    @Json(name = "from_id") val fromId: Int,
    @Json(name = "position") val position: Int,
    @Json(name = "attachment") val attachment: VkAttachmentItemData
) {

    fun toDomain(): VkAttachmentHistoryMessage = VkAttachmentHistoryMessage(
        messageId = messageId,
        conversationMessageId = conversationMessageId,
        date = date,
        fromId = fromId,
        position = position,
        attachment = attachment.toDomain()
    )
}
