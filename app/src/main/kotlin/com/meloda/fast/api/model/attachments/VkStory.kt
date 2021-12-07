package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkStory(
    val id: Int,
    val ownerId: Int,
    val date: Int,
    val photo: VkPhoto?
) : VkAttachment()