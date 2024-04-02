package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

data class VkGift(
    val id: Int,
    val thumb256: String?,
    val thumb96: String?,
    val thumb48: String
) : VkAttachment() {

    val className: String = this::class.java.name
}
