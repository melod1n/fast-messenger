package dev.meloda.fast.model.api.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkLongPollData(
    @Json(name = "server") val server: String,
    @Json(name = "key") val key: String,
    @Json(name = "ts") val ts: Int,
    @Json(name = "pts") val pts: Int
)
