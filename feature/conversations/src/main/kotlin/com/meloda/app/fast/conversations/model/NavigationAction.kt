package com.meloda.app.fast.conversations.model

sealed class NavigationAction {

    data class NavigateToMessagesHistory(val conversationId: Int) : NavigationAction()
    data object NavigateToSettings : NavigationAction()
}
