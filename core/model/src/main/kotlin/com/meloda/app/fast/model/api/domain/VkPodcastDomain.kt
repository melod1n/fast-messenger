package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkPodcastDomain(
    val id: Int,
    val title: String,
    val artist: String
) : VkAttachment {
    override val type: AttachmentType = AttachmentType.PODCAST
}
