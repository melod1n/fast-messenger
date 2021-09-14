package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkWallReply(
    val id: Int
) : VkAttachment()