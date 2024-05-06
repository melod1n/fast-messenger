package com.meloda.app.fast.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "error") val error: RestApiError?,
    @Json(name = "response") val response: T?
) {
    val isSuccessful get() = error == null && response != null

    fun requireResponse(): T = requireNotNull(response)
}
