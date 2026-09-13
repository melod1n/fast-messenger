package dev.meloda.fast.profile.model

import dev.meloda.fast.model.api.domain.VkUser

data class ProfileScreenState(
    val isLoading: Boolean = false,
    val isCurrentUser: Boolean = false,
    val user: VkUser? = null,
    val isError: Boolean = false
) {
    val avatarUrl: String? get() = user?.photo400Orig ?: user?.photo200 ?: user?.photo100
    val fullName: String? get() = user?.fullName

    companion object {
        val EMPTY: ProfileScreenState = ProfileScreenState(
            isLoading = false,
            isCurrentUser = false,
            user = null,
            isError = false
        )
    }
}
