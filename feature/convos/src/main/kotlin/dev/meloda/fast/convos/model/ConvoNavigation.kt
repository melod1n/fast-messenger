package dev.meloda.fast.convos.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ConvoNavigation {

    data class MessagesHistory(val peerId: Long) : ConvoNavigation()

    data object CreateChat : ConvoNavigation()
}
