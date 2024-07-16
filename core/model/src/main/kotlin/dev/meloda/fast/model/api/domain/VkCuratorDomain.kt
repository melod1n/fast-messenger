package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkCuratorDomain(
    val id: Int,
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.CURATOR
}
