package com.meloda.fast.api.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

interface OauthError

@JsonClass(generateAdapter = true)
open class BaseOauthError(
    @Json(name = "error") open val error: String,
    @Json(name = "error_description") open val errorDescription: String?,
    @Json(name = "error_type") open val errorType: String?
) : OauthError

@JsonClass(generateAdapter = true)
data class ValidationRequiredError(
    @Json(name = "error") val error: String, // "need_validation"
    @Json(name = "error_description") val errorDescription: String, // "sms sent, use code param" if sms method; "use app code" if 2fa app
    @Json(name = "validation_type") val validationType: String, // 2fa_app, 2sa_sms
    @Json(name = "validation_sid") val validationSid: String,
    @Json(name = "phone_mask") val phoneMask: String, // "+7 *** *** ** 50"
    @Json(name = "redirect_uri") val redirectUri: String,
    @Json(name = "validation_resend") val validationResend: String, // Приходит, если для отправки кода нужно вызвать метод auth.validatePhone
    @Json(name = "cant_get_code_open_restore") val restoreIfCannotGetCode: Boolean?
) : OauthError

@JsonClass(generateAdapter = true)
data class CaptchaRequiredError(
    @Json(name = "error") val error: String, // "need_captcha"
    @Json(name = "captcha_sid") val captchaSid: String,
    @Json(name = "captcha_img") val captchaImage: String,
    @Json(name = "captcha_ts") val captchaTs: Double,
    @Json(name = "captcha_ratio") val captchaRatio: Double,
    @Json(name = "captcha_track") val captchaTrack: String,
    @Json(name = "is_refresh_enabled") val isRefreshEnabled: Boolean,
    @Json(name = "is_sound_captcha_available") val isSoundCaptchaAvailable: Boolean
) : OauthError

@JsonClass(generateAdapter = true)
data class UserBannedError(
    @Json(name = "error") val error: String, // need_validation
    @Json(name = "error_description") val errorDescription: String, // user has been banned
    @Json(name = "ban_info") val banInfo: BanInfo
) : OauthError {

    @JsonClass(generateAdapter = true)
    data class BanInfo(
        @Json(name = "member_name") val memberName: String,
        @Json(name = "message") val message: String,
        @Json(name = "access_token") val accessToken: String,
        @Json(name = "restore_url") val restoreUrl: String
    )
}

@JsonClass(generateAdapter = true)
data class InvalidCredentialsError(
    @Json(name = "error") val error: String, // "invalid_client"
    @Json(name = "error_description") val errorDescription: String,
    @Json(name = "error_type") val errorType: String // "username_or_password_is_incorrect"
) : OauthError

@JsonClass(generateAdapter = true)
data class WrongTwoFaCode(
    @Json(name = "error") val error: String, // "invalid_request"
    @Json(name = "error_description") val errorDescription: String,
    @Json(name = "error_type") val errorType: String // "wrong_otp"
) : OauthError
