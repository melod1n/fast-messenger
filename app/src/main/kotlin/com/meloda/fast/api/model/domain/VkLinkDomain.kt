package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkLinkDomain(
    val url: String,
    val title: String?,
    val caption: String?,
    val photo: VkPhotoDomain?,
    val target: String?,
    val isFavorite: Boolean
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.LINK

    val className: String = this::class.java.name

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_link)
}
