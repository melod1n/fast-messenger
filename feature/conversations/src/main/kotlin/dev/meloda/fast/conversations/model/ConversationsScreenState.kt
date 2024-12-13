package dev.meloda.fast.conversations.model

import androidx.compose.runtime.Immutable

@Immutable
data class ConversationsScreenState(
    val showOptions: ConversationsShowOptions,
    val conversations: List<UiConversation>,
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val profileImageUrl: String?,
    val scrollIndex: Int,
    val scrollOffset: Int
) {

    companion object {
        val EMPTY: ConversationsScreenState = ConversationsScreenState(
            showOptions = ConversationsShowOptions.EMPTY,
            conversations = emptyList(),
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            profileImageUrl = null,
            scrollIndex = 0,
            scrollOffset = 0,
        )
    }
}
