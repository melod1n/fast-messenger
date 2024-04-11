package com.meloda.fast.api.network.oauth

import com.meloda.fast.BuildConfig
import com.meloda.fast.api.VKConstants

data class AuthDirectRequest(
    val grantType: String,
    val clientId: String,
    val clientSecret: String,
    val username: String,
    val password: String,
    val scope: String,
    val twoFaSupported: Boolean = true,
    val twoFaForceSms: Boolean = false,
    val twoFaCode: String? = null,
    val captchaSid: String? = null,
    val captchaKey: String? = null,
    val trustedHash: String? = null
) {

    val map
        get() = mutableMapOf(
            "grant_type" to grantType,
            "client_id" to clientId,
            "client_secret" to clientSecret,
            "username" to username,
            "password" to password,
            "scope" to scope,
            "2fa_supported" to if (twoFaSupported) "1" else "0",
            "force_sms" to if (twoFaForceSms) "1" else "0"
        )
            .apply {
                twoFaCode?.let { this["code"] = it }
                captchaSid?.let { this["captcha_sid"] = it }
                captchaKey?.let { this["captcha_key"] = it }
                trustedHash?.let { this["trusted_hash"] = it }
            }
}

data class AuthWithAppRequest(
    val redirectUrl: String = "https://oauth.vk.com/blank.html",
    val display: String = "page",
    val responseType: String = "token",
    val accessToken: String,
    val revoke: Int = 1,
    val scope: Int = 136297695,
    val clientId: String = VKConstants.FAST_APP_ID,
    val sdkPackage: String = BuildConfig.sdkPackage,
    val sdkFingerprint: String = BuildConfig.sdkFingerprint
) {

    val map
        get() = mutableMapOf(
            "redirect_url" to redirectUrl,
            "display" to display,
            "response_type" to responseType,
            "access_token" to accessToken,
            "revoke" to revoke.toString(),
            "scope" to scope.toString(),
            "client_id" to clientId,
            "sdk_package" to sdkPackage,
            "sdk_fingerprint" to sdkFingerprint
        )

}
