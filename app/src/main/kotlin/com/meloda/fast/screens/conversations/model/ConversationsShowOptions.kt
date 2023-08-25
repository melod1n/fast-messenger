package com.meloda.fast.screens.conversations.model

import com.meloda.fast.api.model.presentation.VkConversationUi

data class ConversationsShowOptions(
    val showOptionsDialog: VkConversationUi?,
    val showDeleteDialog: Int?,
    val showPinDialog: VkConversationUi?
) {

    companion object {
        val EMPTY: ConversationsShowOptions = ConversationsShowOptions(
            showOptionsDialog = null,
            showDeleteDialog = null,
            showPinDialog = null
        )
    }
}
