package dev.meloda.fast.conversations.model

data class ConversationsShowOptions(
    val showDeleteDialog: Int?,
    val showPinDialog: UiConversation?
) {

    companion object {
        val EMPTY: ConversationsShowOptions = ConversationsShowOptions(
            showDeleteDialog = null,
            showPinDialog = null
        )
    }
}
