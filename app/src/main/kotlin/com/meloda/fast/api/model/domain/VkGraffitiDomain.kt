package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkGraffitiDomain(
    val id: Int,
    val ownerId: Int,
    val url: String,
    val width: Int,
    val height: Int,
    val accessKey: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GRAFFITI

    val className: String = this::class.java.name

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_graffiti)
}
