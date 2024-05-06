package com.meloda.app.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthDirectResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "trusted_hash") val twoFaHash: String?,
    @Json(name = "validation_sid") val validationSid: String?,
    @Json(name = "validation_type") val validationType: String?,
    @Json(name = "phone_mask") val phoneMask: String?,
    @Json(name = "redirect_uri") val redirectUrl: String?,
    @Json(name = "validation_resend") val validationResend: String?,
    @Json(name = "cant_get_code_open_restore") val restoreIfCannotGetCode: Boolean?
)
