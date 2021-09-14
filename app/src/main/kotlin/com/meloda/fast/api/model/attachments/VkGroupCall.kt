package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkGroupCall(
    val initiatorId: Int
) : VkAttachment()