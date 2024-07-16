package dev.meloda.fast.profile.model

data class ProfileScreenState(
    val isLoading: Boolean,
    val avatarUrl: String?,
    val fullName: String?
) {

    companion object {
        val EMPTY: ProfileScreenState = ProfileScreenState(
            isLoading = false,
            avatarUrl = null,
            fullName = null
        )
    }
}
