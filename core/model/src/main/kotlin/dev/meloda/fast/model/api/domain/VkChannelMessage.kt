package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkChannelMessage(
    val channelId: Long,
    val cmId: Long,
    val authorId: Long,
    val channelInfo: ChannelInfo,
    val channelType: String,
    val guid: String,
    val text: String?,
    val time: Long,
    val attachments: List<VkAttachment>?,
) : VkAttachment {

    data class ChannelInfo(
        val title: String,
        val photoBase: String?
    )

    override val type: AttachmentType = AttachmentType.CHANNEL_MESSAGE
}
