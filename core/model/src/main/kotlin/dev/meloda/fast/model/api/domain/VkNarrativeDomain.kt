package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkNarrativeDomain(
    val id: Int,
    val title: String?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.NARRATIVE
}
