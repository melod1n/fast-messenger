package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkCallDomain(
    val initiatorId: Int,
    val receiverId: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val isVideo: Boolean
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.CALL
}
