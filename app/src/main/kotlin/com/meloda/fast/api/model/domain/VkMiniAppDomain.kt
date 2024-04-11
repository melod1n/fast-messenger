package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkMiniAppDomain(
    val link: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.MINI_APP

    val className: String = this::class.java.name

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_mini_app)
}
