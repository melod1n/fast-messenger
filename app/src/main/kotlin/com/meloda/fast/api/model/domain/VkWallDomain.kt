package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.api.model.data.VkAttachmentItemData
import com.meloda.fast.model.base.UiText

data class VkWallDomain(
    val id: Int,
    val fromId: Int,
    val toId: Int,
    val date: Int,
    val text: String,
    val attachments: List<VkAttachmentItemData>?,
    val comments: Int?,
    val likes: Int?,
    val reposts: Int?,
    val views: Int?,
    val isFavorite: Boolean,
    val accessKey: String?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.WALL

    val className: String = this::class.java.name

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_wall)
}
