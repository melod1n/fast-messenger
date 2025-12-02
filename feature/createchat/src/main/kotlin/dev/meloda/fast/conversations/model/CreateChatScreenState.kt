package dev.meloda.fast.conversations.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.ui.model.api.UiFriend

@Immutable
data class CreateChatScreenState(
    val isLoading: Boolean,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val friends: List<UiFriend>,
    val selectedFriendsIds: List<Long>,
    val chatTitle: String,
    val showConfirmDialog: Boolean
) {
    companion object {
        val EMPTY: CreateChatScreenState = CreateChatScreenState(
            isLoading = true,
            isPaginating = false,
            isPaginationExhausted = false,
            friends = emptyList(),
            selectedFriendsIds = emptyList(),
            chatTitle = "",
            showConfirmDialog = false
        )
    }
}
