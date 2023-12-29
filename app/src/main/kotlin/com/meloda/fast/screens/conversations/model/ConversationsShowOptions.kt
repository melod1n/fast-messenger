package com.meloda.fast.screens.conversations.model

import com.meloda.fast.api.model.presentation.VkConversationUi

data class ConversationsShowOptions(
    val showDeleteDialog: Int?,
    val showPinDialog: VkConversationUi?
) {

    companion object {
        val EMPTY: ConversationsShowOptions = ConversationsShowOptions(
            showDeleteDialog = null,
            showPinDialog = null
        )
    }
}
