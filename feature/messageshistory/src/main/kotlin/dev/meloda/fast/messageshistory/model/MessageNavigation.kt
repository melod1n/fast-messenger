package dev.meloda.fast.messageshistory.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class MessageNavigation {

    data class ChatMaterials(
        val peerId: Long,
        val cmId: Long
    ) : MessageNavigation()
}
