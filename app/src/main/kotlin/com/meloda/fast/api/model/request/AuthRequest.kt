package com.meloda.fast.api.model.request

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RequestAuthDirect(
    @SerializedName("grant_type") val grantType: String,
    @SerializedName("client_id") val clientId: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("scope") val scope: String,
    @SerializedName("2fa_supported") val twoFaSupported: Boolean = true,
    @SerializedName("force_sms") val twoFaForceSms: Boolean = false,
    @SerializedName("code") val twoFaCode: String? = null,
    @SerializedName("captcha_sid") val captchaSid: String? = null,
    @SerializedName("captcha_key") val captchaKey: String? = null,
) : Parcelable {
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
            }
}