package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkWidget(
    val id: Int
) : VkAttachment()
