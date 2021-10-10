package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkLink(
    val url: String,
    val title: String?,
    val caption: String?,
    val photo: VkPhoto?,
    val target: String?,
    val isFavorite: Boolean
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name
}
