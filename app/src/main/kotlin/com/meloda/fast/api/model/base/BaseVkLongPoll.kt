package com.meloda.fast.api.model.base

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BaseVkLongPoll(
    val server: String,
    val key: String,
    val ts: Int,
    val pts: Int
)
