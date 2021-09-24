package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkAudio(
    val id: Int,
    val title: String,
    val artist: String,
    val url: String,
    val duration: Int
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name
}