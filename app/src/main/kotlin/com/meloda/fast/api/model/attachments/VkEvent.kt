package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkEvent(
    val id: Int
) : VkAttachment()