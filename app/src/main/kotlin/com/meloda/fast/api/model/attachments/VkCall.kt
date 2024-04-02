package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

data class VkCall(
    val initiatorId: Int,
    val receiverId: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val isVideo: Boolean
) : VkAttachment() {

    val className: String = this::class.java.name

}
