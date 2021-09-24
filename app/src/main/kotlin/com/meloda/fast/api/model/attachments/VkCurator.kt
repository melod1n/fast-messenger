package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkCurator(
    val id: Int
) : VkAttachment()