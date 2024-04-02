package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

data class VkWallReply(
    val id: Int
) : VkAttachment() {

    val className: String = this::class.java.name
}
