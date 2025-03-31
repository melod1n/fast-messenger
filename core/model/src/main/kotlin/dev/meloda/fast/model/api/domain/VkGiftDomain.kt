package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkGiftDomain(
    val id: Long,
    val thumb256: String?,
    val thumb96: String?,
    val thumb48: String
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GIFT
}
