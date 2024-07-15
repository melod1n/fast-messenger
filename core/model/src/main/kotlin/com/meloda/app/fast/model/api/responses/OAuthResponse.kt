package com.meloda.app.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthDirectErrorOnlyResponse(
    @Json(name = "error") val error: String,
    @Json(name = "error_description") val errorDescription: String?,
    @Json(name = "error_type") val errorType: String?
)

@JsonClass(generateAdapter = true)
data class AuthDirectResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "user_id") val userId: Int?,
    @Json(name = "trusted_hash") val validationHash: String?,
    @Json(name = "validation_sid") val validationSid: String?,
    @Json(name = "validation_type") val validationType: String?,
    @Json(name = "phone_mask") val phoneMask: String?,
    @Json(name = "redirect_uri") val redirectUri: String?,
    @Json(name = "validation_resend") val validationResend: String?,
    @Json(name = "cant_get_code_open_restore") val restoreIfCannotGetCode: Boolean?,
    @Json(name = "error") val error: String?,
    @Json(name = "error_description") val errorDescription: String?,
    @Json(name = "error_type") val errorType: String?,
    @Json(name = "ban_info") val banInfo: BanInfo?,
    @Json(name = "captcha_sid") val captchaSid: String?,
    @Json(name = "captcha_img") val captchaImage: String?,
    @Json(name = "captcha_ts") val captchaTs: Double?,
    @Json(name = "captcha_ratio") val captchaRatio: Double?,
    @Json(name = "captcha_track") val captchaTrack: String?,
    @Json(name = "is_refresh_enabled") val isRefreshEnabled: Boolean?,
    @Json(name = "is_sound_captcha_available") val isSoundCaptchaAvailable: Boolean?
) {

    @JsonClass(generateAdapter = true)
    data class BanInfo(
        @Json(name = "member_name") val memberName: String,
        @Json(name = "message") val message: String,
        @Json(name = "access_token") val accessToken: String,
        @Json(name = "restore_url") val restoreUrl: String
    )
}

@JsonClass(generateAdapter = true)
data class GetAnonymousTokenResponse(
    @Json(name = "token") val token: String,
    @Json(name = "expired_at") val expiredAt: Int
)
