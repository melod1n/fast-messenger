package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkGroupCallDomain(
    val initiatorId: Int
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.GROUP_CALL_IN_PROGRESS
}
