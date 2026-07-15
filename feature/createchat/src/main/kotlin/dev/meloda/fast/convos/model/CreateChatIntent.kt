package dev.meloda.fast.convos.model

sealed class CreateChatIntent {
    data object Back : CreateChatIntent()
    data object Refresh : CreateChatIntent()
    data object PaginationConditionsMet : CreateChatIntent()
    data class TitleInput(val input: String) : CreateChatIntent()
    data object CreateChatButtonClick : CreateChatIntent()
    data class ListItemClick(val id: Long) : CreateChatIntent()
    data class RemoveUserClick(val id: Long) : CreateChatIntent()

    sealed class Dialog : CreateChatIntent() {
        data object Dismiss : Dialog()
        data object ConfirmClick : Dialog()
    }
}
