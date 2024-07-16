package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LongPollUpdates(
    @Json(name = "failed") val failed: Int?,
    @Json(name = "ts") val ts: Int?,
    @Json(name = "pts") val pts: Int?,
    // TODO: 14/05/2024, Danil Nikolaev: List<List<Any>>?????
    @Json(name = "updates") val updates: List<List<Any>>?,
    @Json(name = "error") val error: String?
)
