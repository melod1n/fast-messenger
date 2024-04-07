package com.meloda.fast.api.model.domain

data class VkCallDomain(
    val initiatorId: Int,
    val receiverId: Int,
    val state: String,
    val time: Int,
    val duration: Int,
    val isVideo: Boolean
) : VkAttachment {

    val className: String = this::class.java.name
}
