package dev.meloda.fast.friends.model

import androidx.compose.runtime.Immutable
import dev.meloda.fast.ui.model.api.UiFriend

@Immutable
data class FriendsScreenState(
    val isLoading: Boolean,
    val friends: List<UiFriend>,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean,
    val scrollIndex: Int,
    val scrollOffset: Int,
    val orderType: String,
) {

    companion object {
        val EMPTY: FriendsScreenState = FriendsScreenState(
            isLoading = true,
            friends = emptyList(),
            isPaginating = false,
            isPaginationExhausted = false,
            scrollIndex = 0,
            scrollOffset = 0,
            orderType = "hints"
        )
    }
}
