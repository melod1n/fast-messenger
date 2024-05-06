package com.meloda.app.fast.conversations.model

sealed class UiAction {

    data class NavigateToMessagesHistory(val conversationId: Int) : UiAction()
    data object NavigateToSettings : UiAction()
    data object CreateChatClicked : UiAction()
}
