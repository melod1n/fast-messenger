package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkGiftDomain(
    val id: Int,
    val thumb256: String?,
    val thumb96: String?,
    val thumb48: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GIFT
}
