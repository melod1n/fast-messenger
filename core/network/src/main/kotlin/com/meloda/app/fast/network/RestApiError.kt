package dev.meloda.fast.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RestApiError(
//    @Json(name = "code") val code: Int?,
//    @Json(name = "message") val message: String?,
    @Json(name = "error_code") val errorCode: Int,
    @Json(name = "error_msg") val errorMsg: String
) {
    fun toDomain(): RestApiErrorDomain = RestApiErrorDomain(
        code = errorCode,
        message = errorMsg
    )
}
