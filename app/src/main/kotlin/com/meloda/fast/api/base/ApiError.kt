package com.meloda.fast.api.base

import com.google.gson.annotations.SerializedName
import com.meloda.fast.base.RestApiErrorDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okio.IOException

open class ApiError(
    @SerializedName("error", alternate = ["error_code"])
    val error: String? = null,
    @SerializedName("error_msg", alternate = ["error_description"])
    open val errorMessage: String? = null,
    @SerializedName("error_type")
    val errorType: String? = null,
    val throwable: Throwable? = null
) : IOException()

@JsonClass(generateAdapter = true)
data class RestApiError(
    @Json(name = "code") val code: Int,
    @Json(name = "message") val message: String
) {
    fun toDomain(): RestApiErrorDomain = RestApiErrorDomain(
        code = code, message = message
    )
}
