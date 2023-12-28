package com.meloda.fast.data.longpoll

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.RawValue

@JsonClass(generateAdapter = false)
data class LongPollUpdates(
    @Json(name = "failed") val failed: Int?,
    @Json(name = "ts") val ts: Int?,
    @Json(name = "pts") val pts: Int?,
    @Json(name = "updates") val updates: @RawValue List<List<Any>>?,
    @Json(name = "error") val error: String?
)