package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkPoll(
    val id: Int
) : VkAttachment()