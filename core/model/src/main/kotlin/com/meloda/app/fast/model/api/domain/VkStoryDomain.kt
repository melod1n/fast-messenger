package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkStoryDomain(
    val id: Int,
    val ownerId: Int,
    val date: Int,
    val photo: VkPhotoDomain?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.STORY
}
