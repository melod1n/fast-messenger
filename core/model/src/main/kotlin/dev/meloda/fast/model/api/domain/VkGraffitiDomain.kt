package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkGraffitiDomain(
    val id: Long,
    val ownerId: Long,
    val url: String,
    val width: Int,
    val height: Int,
    val accessKey: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GRAFFITI
}
