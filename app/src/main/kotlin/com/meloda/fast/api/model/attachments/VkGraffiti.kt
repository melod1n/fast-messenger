package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkGraffiti(
    val link: String
) : VkAttachment()