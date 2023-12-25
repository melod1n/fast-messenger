package com.meloda.fast.screens.conversations.model

import androidx.compose.runtime.Immutable
import com.meloda.fast.api.model.presentation.VkConversationUi

@Immutable
data class ConversationsScreenState(
    val showOptions: ConversationsShowOptions,
    val conversations: List<VkConversationUi>,
    val isLoading: Boolean,
    val pinnedConversationsCount: Int,
) {

    companion object {
        val EMPTY: ConversationsScreenState = ConversationsScreenState(
            showOptions = ConversationsShowOptions.EMPTY,
            conversations = emptyList(),
            isLoading = true,
            pinnedConversationsCount = 0,
        )
    }
}
