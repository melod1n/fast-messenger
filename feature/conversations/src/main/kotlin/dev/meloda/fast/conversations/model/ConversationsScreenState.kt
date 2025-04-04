package dev.meloda.fast.conversations.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.ui.model.api.UiConversation

@Immutable
data class ConversationsScreenState(
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val profileImageUrl: String?,
    val scrollIndex: Int,
    val scrollOffset: Int,
    val isArchive: Boolean
) {

    companion object {
        val EMPTY: ConversationsScreenState = ConversationsScreenState(
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            profileImageUrl = null,
            scrollIndex = 0,
            scrollOffset = 0,
            isArchive = false
        )
    }
}
