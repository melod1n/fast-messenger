package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.api.model.data.VkArtistData
import com.meloda.fast.model.base.UiText

data class VkArtistDomain(
    val id: String,
    val name: String,
    val photos: List<VkArtistData.PhotoSize>
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.ARTIST

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_artist)
}
