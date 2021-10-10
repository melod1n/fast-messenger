package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkGraffiti(
    val id: Int,
    val ownerId: Int,
    val url: String,
    val width: Int,
    val height: Int,
    val accessKey: String
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name
}