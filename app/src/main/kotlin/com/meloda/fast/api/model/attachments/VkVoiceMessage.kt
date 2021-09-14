package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkVoiceMessage(
    val link: String
) : VkAttachment()
