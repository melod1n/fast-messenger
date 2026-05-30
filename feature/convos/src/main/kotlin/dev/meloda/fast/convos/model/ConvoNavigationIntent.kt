package dev.meloda.fast.convos.model

sealed class ConvoNavigationIntent {

    data object Back : ConvoNavigationIntent()
    data class MessagesHistory(val convoId: Long) : ConvoNavigationIntent()
    data object CreateChat : ConvoNavigationIntent()
    data object Archive : ConvoNavigationIntent()
}
