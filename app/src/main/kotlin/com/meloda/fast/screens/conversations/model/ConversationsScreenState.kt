package com.meloda.fast.screens.conversations.model

import com.meloda.fast.api.model.presentation.VkConversationUi
import com.meloda.fast.screens.settings.SettingsKeys

data class ConversationsScreenState(
    val showOptions: ConversationsShowOptions,
    val conversations: List<VkConversationUi>,
    val isLoading: Boolean,
    val multilineEnabled: Boolean,
    val pinnedConversationsCount: Int
) {

    companion object {
        val EMPTY: ConversationsScreenState = ConversationsScreenState(
            showOptions = ConversationsShowOptions.EMPTY,
            conversations = emptyList(),
            isLoading = true,
            multilineEnabled = SettingsKeys.DEFAULT_VALUE_MULTILINE,
            pinnedConversationsCount = 0
        )
    }
}
