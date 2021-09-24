package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkFile(
    val id: Int,
    val title: String,
    val ext: String,
    val size: Int,
    val url: String
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name
}