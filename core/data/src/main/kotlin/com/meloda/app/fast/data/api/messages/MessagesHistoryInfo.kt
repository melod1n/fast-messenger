package com.meloda.app.fast.data.api.messages

import com.meloda.app.fast.model.api.domain.VkConversation
import com.meloda.app.fast.model.api.domain.VkMessage

data class MessagesHistoryInfo(
    val messages: List<VkMessage>,
    val conversations: List<VkConversation>
)
