package com.meloda.fast.api.base

import com.meloda.fast.api.network.OauthError
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OauthResponse<T>(
    @Json(name = "error") val error: OauthError?,
    @Json(name = "response") val response: T?
) {
    val isSuccessful get() = error == null && response != null
}
