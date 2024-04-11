package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkGiftDomain(
    val id: Int,
    val thumb256: String?,
    val thumb96: String?,
    val thumb48: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GIFT

    val className: String = this::class.java.name

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_gift)
}
