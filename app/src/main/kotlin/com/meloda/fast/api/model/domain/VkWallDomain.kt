package com.meloda.fast.api.model.domain

import com.meloda.fast.api.model.data.VkAttachmentItemData

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

    val className: String = this::class.java.name
}
