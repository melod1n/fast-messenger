package dev.meloda.fast.auth.login.model

data class AuthInfo(
    val userId: Int?,
    val accessToken: String?,
    val validationHash: String?
)
