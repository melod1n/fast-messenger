package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkStoryDomain(
    val id: Int,
    val ownerId: Int,
    val date: Int,
    val photo: VkPhotoDomain?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.STORY

    val className: String = this::class.java.name

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_story)
}
