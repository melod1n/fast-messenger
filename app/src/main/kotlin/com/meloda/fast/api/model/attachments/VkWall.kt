package com.meloda.fast.api.model.attachments

import com.meloda.fast.api.model.base.attachments.BaseVkAttachmentItem
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkWall(
    val id: Int,
    val fromId: Int,
    val toId: Int,
    val date: Int,
    val text: String,
    val attachments: List<BaseVkAttachmentItem>?,
    val comments: Int?,
    val likes: Int?,
    val reposts: Int?,
    val views: Int?,
    val isFavorite: Boolean,
    val accessKey: String?
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name
}
