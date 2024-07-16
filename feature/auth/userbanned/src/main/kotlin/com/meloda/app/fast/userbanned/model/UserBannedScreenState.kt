package dev.meloda.fast.userbanned.model

data class UserBannedScreenState(
    val userName: String,
    val message: String
) {

    companion object {
        val EMPTY: UserBannedScreenState = UserBannedScreenState(
            userName = "",
            message = ""
        )
    }
}
