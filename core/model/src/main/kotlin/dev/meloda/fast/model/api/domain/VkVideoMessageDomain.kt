package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkVideoMessageDomain(
    val id: Long
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.VIDEO_MESSAGE
}
