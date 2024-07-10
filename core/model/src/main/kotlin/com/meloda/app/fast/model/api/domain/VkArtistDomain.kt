package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType
import com.meloda.app.fast.model.api.data.VkArtistData

data class VkArtistDomain(
    val id: String,
    val name: String,
    val photos: List<VkArtistData.PhotoSize>
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.ARTIST
}
