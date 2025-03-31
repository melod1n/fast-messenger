package dev.meloda.fast.ui.model.api

data class ConversationsShowOptions(
    val showDeleteDialog: Long?,
    val showPinDialog: UiConversation?
) {

    companion object {
        val EMPTY: ConversationsShowOptions = ConversationsShowOptions(
            showDeleteDialog = null,
            showPinDialog = null
        )
    }
}
