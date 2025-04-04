package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkStoryDomain(
    val id: Long,
    val ownerId: Long,
    val date: Int,
    val photo: VkPhotoDomain?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.STORY
}
