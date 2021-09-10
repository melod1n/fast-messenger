package com.meloda.fast.api.model

data class VkMessage(
    val id: Int,
    val text: String?,
    val isOut: Boolean,
    val peerId: Int,
    val fromId: Int,
    val date: Int
)
