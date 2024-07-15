package com.meloda.app.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidatePhoneResponse(
    @Json(name = "sid") val validationSid: String?,
    @Json(name = "delay") val delay: Int?,
    @Json(name = "validation_type") val validationType: String?,
    @Json(name = "validation_resend") val validationResend: String?
)

@JsonClass(generateAdapter = true)
data class ValidateLoginResponse(
    @Json(name = "result") val result: String,
    @Json(name = "sid") val sid: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "is_email") val isEmail: Boolean?,
    @Json(name = "email_reg_allowed") val emailRegAllowed: Boolean?
)
