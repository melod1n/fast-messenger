package com.meloda.app.fast.friends.model

import androidx.compose.runtime.Immutable

@Immutable
data class FriendsScreenState(
    val isLoading: Boolean,
    val friends: List<UiFriend>,
    val onlineFriends: List<UiFriend>,
    val isPaginating: Boolean,
    val isPaginationExhausted: Boolean
) {

    companion object {
        val EMPTY: FriendsScreenState = FriendsScreenState(
            isLoading = true,
            friends = emptyList(),
            onlineFriends = emptyList(),
            isPaginating = false,
            isPaginationExhausted = false
        )
    }
}
