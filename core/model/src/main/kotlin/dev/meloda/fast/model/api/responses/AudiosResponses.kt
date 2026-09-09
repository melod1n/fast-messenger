package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AudiosGetUploadServerResponse(
    @Json(name = "upload_url")
    val uploadUrl: String
)

@JsonClass(generateAdapter = true)
data class AudiosUploadResponse(
    val redirect: String? = null,
    val server: Int? = null,
    val audio: String? = null,
    val hash: String? = null,
    val error: String? = null
)
