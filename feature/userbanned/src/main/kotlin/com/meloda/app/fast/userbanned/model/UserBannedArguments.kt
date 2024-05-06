package com.meloda.app.fast.userbanned.model

data class UserBannedArguments(
    val name: String,
    val message: String,
    val restoreUrl: String,
    val accessToken: String
)
