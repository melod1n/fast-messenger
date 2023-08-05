package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkWidget(
    val id: Int
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name
}
