package dev.meloda.fast.model

data class AuthInfo(
    val userId: Int?,
    val accessToken: String?,
    val validationHash: String?
)
