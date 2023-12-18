package com.meloda.fast.screens.conversations.model

import com.meloda.fast.api.model.presentation.ConversationsList

data class ConversationsScreenState(
    val showOptions: ConversationsShowOptions,
    val conversations: ConversationsList,
    val isLoading: Boolean,
    val pinnedConversationsCount: Int,
) {

    companion object {
        val EMPTY: ConversationsScreenState = ConversationsScreenState(
            showOptions = ConversationsShowOptions.EMPTY,
            conversations = ConversationsList.EMPTY,
            isLoading = true,
            pinnedConversationsCount = 0,
        )
    }
}
