package com.meloda.app.fast.model.api.requests

data class AuthDirectRequest(
    val grantType: String,
    val clientId: String,
    val clientSecret: String,
    val username: String,
    val password: String,
    val scope: String,
    val validationSupported: Boolean = true,
    val validationForceSms: Boolean = false,
    val validationCode: String? = null,
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
            "2fa_supported" to if (validationSupported) "1" else "0",
            "force_sms" to if (validationForceSms) "1" else "0"
        )
            .apply {
                validationCode?.let { this["code"] = it }
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
    val clientId: String,
    val sdkPackage: String,
    val sdkFingerprint: String
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
