package com.meloda.app.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LongPollUpdates(
    @Json(name = "failed") val failed: Int?,
    @Json(name = "ts") val ts: Int?,
    @Json(name = "pts") val pts: Int?,
    @Json(name = "updates") val updates: List<List<Any>>?,
    @Json(name = "error") val error: String?
)
