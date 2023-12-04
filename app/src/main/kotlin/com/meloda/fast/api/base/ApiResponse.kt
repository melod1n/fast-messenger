package com.meloda.fast.api.base

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "error") val error: ApiError? = null,
    @Json(name = "response") val response: T? = null
) {
    val isSuccessful get() = error == null && response != null
}
