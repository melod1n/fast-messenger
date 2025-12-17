package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkAttachmentHistoryMessage

@JsonClass(generateAdapter = true)
data class VkAttachmentHistoryMessageData(
    @Json(name = "message_id") val messageId: Long,
    @Json(name = "date") val date: Int,
    @Json(name = "cmid") val cmId: Long,
    @Json(name = "from_id") val fromId: Long,
    @Json(name = "position") val position: Int,
    @Json(name = "attachment") val attachment: VkAttachmentItemData
) {

    fun toDomain(): VkAttachmentHistoryMessage = VkAttachmentHistoryMessage(
        messageId = messageId,
        cmId = cmId,
        date = date,
        fromId = fromId,
        position = position,
        attachment = attachment.toDomain()
    )
}
