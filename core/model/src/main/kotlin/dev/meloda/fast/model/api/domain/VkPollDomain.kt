package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkPollDomain(
    val id: Long
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.POLL
}
