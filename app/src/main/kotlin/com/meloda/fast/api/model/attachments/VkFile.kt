package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkFile(
    val link: String
) : VkAttachment()