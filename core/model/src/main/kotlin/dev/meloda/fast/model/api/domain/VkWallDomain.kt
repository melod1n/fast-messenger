package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.data.VkAttachmentItemData

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
}
