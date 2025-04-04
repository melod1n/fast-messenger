package dev.meloda.fast.conversations.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class ConversationDialog {
    data class ConversationPin(val conversationId: Long) : ConversationDialog()
    data class ConversationUnpin(val conversationId: Long) : ConversationDialog()
    data class ConversationDelete(val conversationId: Long) : ConversationDialog()
    data class ConversationArchive(val conversationId: Long) : ConversationDialog()
    data class ConversationUnarchive(val conversationId: Long) : ConversationDialog()
}
