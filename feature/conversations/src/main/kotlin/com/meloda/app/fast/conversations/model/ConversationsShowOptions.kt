package com.meloda.app.fast.conversations.model

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
