package com.meloda.fast.api.base

import com.meloda.fast.base.RestApiErrorDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RestApiError(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String
) {
    fun toDomain(): RestApiErrorDomain = RestApiErrorDomain(
        code = code, message = message
    )
}
