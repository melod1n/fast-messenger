package dev.meloda.fast.conversations.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ConversationNavigation {

    data class MessagesHistory(val peerId: Long) : ConversationNavigation()

    data object CreateChat : ConversationNavigation()
}
