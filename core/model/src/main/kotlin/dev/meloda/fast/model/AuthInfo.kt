package dev.meloda.fast.model

data class AuthInfo(
    val userId: Long,
    val accessToken: String,
    val validationHash: String
)
