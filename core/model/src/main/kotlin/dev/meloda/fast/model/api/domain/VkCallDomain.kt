package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkCallDomain(
    val initiatorId: Long,
    val receiverId: Long,
    val state: String,
    val time: Int,
    val duration: Int,
    val isVideo: Boolean
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.CALL
}
