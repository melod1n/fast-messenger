package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkPodcastDomain(
    val id: Long,
    val title: String,
    val artist: String
) : VkAttachment {
    override val type: AttachmentType = AttachmentType.PODCAST
}
