package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.domain.VkChannelMessage

@JsonClass(generateAdapter = true)
data class VkChannelMessageData(
    @Json(name = "channel_id") val channelId: Long,
    @Json(name = "cmid") val cmId: Long,
    @Json(name = "author_id") val authorId: Long,
    @Json(name = "channel_info") val channelInfo: ChannelInfo,
    @Json(name = "channel_type") val channelType: String,
    @Json(name = "guid") val guid: String,
    @Json(name = "text") val text: String?,
    @Json(name = "time") val time: Long,
    @Json(name = "attachments") val attachments: List<VkAttachmentItemData> = emptyList(),
) : VkAttachmentData {

    @JsonClass(generateAdapter = true)
    data class ChannelInfo(
        @Json(name = "photo_base") val photoBase: String?,
        @Json(name = "title") val title: String
    )

    fun toDomain(): VkChannelMessage = VkChannelMessage(
        channelId = channelId,
        cmId = cmId,
        authorId = authorId,
        channelInfo = VkChannelMessage.ChannelInfo(
            title = channelInfo.title,
            photoBase = channelInfo.photoBase
        ),
        channelType = channelType,
        guid = guid,
        text = text,
        time = time,
        attachments = attachments.map(VkAttachmentItemData::toDomain),
    )
}
