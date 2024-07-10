package com.meloda.app.fast.model.api.responses

import com.squareup.moshi.Json

data class AudiosGetUploadServerResponse(
    @Json(name = "upload_url")
    val uploadUrl: String
)

data class AudiosUploadResponse(
    val redirect: String,
    val server: Int,
    val audio: String?,
    val hash: String,
    val error: String?
)
