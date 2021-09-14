package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkSticker(
    val link: String
) : VkAttachment()
