package com.meloda.fast.api.model

data class VkConversation(
    val id: Int,
    val title: String?,
    val lastMessage: VkMessage
)
