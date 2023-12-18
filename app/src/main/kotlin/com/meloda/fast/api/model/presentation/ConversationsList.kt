package com.meloda.fast.api.model.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class ConversationsList(
    val conversations: List<VkConversationUi>
) {
    val size get() = conversations.size

    companion object {
        val EMPTY = ConversationsList(conversations = emptyList())
    }
}
