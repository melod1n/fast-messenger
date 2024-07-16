package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.data.VkArtistData

data class VkArtistDomain(
    val id: String,
    val name: String,
    val photos: List<VkArtistData.PhotoSize>
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.ARTIST
}
