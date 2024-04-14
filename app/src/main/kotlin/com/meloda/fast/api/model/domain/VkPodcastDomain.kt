package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkPodcastDomain(
    val id: Int,
    val title: String,
    val artist: String
) : VkAttachment {
    override val type: AttachmentType = AttachmentType.PODCAST

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_podcast)
}
