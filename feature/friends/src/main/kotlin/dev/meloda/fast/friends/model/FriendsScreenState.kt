package dev.meloda.fast.friends.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.ui.model.api.UiFriend

@Immutable
data class FriendsScreenState(
    val isLoading: Boolean,
    val friends: List<UiFriend>,
    val onlineFriends: List<UiFriend>,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val selectedTabIndex: Int,
    val scrollIndex: Int,
    val scrollOffset: Int,
    val scrollIndexOnline: Int,
    val scrollOffsetOnline: Int
) {

    companion object {
        val EMPTY: FriendsScreenState = FriendsScreenState(
            isLoading = true,
            friends = emptyList(),
            onlineFriends = emptyList(),
            isPaginating = false,
            isPaginationExhausted = false,
            selectedTabIndex = 0,
            scrollIndex = 0,
            scrollOffset = 0,
            scrollIndexOnline = 0,
            scrollOffsetOnline = 0,
        )
    }
}
