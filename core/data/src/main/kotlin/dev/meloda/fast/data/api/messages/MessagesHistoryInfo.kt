package dev.meloda.fast.data.api.messages

import dev.meloda.fast.model.api.domain.VkConversation
import dev.meloda.fast.model.api.domain.VkMessage

data class MessagesHistoryInfo(
    val messages: List<VkMessage>,
    val conversations: List<VkConversation>
)
