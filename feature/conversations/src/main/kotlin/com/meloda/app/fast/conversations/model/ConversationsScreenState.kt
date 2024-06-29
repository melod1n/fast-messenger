package com.meloda.app.fast.conversations.model

import androidx.compose.runtime.Immutable

@Immutable
data class ConversationsScreenState(
    val showOptions: ConversationsShowOptions,
    val conversations: List<UiConversation>,
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
