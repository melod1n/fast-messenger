package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.Parcelize

@Parcelize
data class VkMiniApp(
    val link: String
) : VkAttachment()
