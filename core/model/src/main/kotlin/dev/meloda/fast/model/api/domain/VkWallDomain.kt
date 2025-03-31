package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType
import dev.meloda.fast.model.api.data.VkAttachmentItemData

data class VkWallDomain(
    val id: Long,
    val fromId: Long,
    val toId: Long,
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
