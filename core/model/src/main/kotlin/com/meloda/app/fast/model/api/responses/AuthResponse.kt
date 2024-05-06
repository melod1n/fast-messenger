package com.meloda.app.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SendSmsResponse(
    @Json(name = "sid") val validationSid: String?,
    @Json(name = "delay") val delay: Int?,
    @Json(name = "validation_type") val validationType: String?,
    @Json(name = "validation_resend") val validationResend: String?
)
