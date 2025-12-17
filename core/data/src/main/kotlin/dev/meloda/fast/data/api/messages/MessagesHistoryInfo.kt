package dev.meloda.fast.data.api.messages

import dev.meloda.fast.model.api.domain.VkConvo
import dev.meloda.fast.model.api.domain.VkMessage

data class MessagesHistoryInfo(
    val messages: List<VkMessage>,
    val convos: List<VkConvo>
)
