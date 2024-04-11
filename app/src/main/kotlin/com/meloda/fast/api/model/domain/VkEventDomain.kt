package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkEventDomain(
    val id: Int
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.EVENT

    val className: String = this::class.java.name

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_event)
}
