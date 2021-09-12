package com.meloda.fast.api.model

import com.meloda.fast.api.model.attachments.VkAttachment

data class VkGroupCall(
    val initiatorId: Int
) : VkAttachment()