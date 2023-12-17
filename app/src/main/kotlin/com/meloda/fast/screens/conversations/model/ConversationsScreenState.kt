package com.meloda.fast.screens.conversations.model

import com.meloda.fast.api.model.presentation.VkConversationUi

data class ConversationsScreenState(
    val showOptions: ConversationsShowOptions,
    val conversations: List<VkConversationUi>,
    val isLoading: Boolean,
    val pinnedConversationsCount: Int,
    val avatars: List<String>
) {

    companion object {
        val EMPTY: ConversationsScreenState = ConversationsScreenState(
            showOptions = ConversationsShowOptions.EMPTY,
            conversations = emptyList(),
            isLoading = true,
            pinnedConversationsCount = 0,
            avatars = emptyList()
        )
    }
}
