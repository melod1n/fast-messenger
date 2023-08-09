package com.meloda.fast.api.network.auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthDirectResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("user_id") val userId: Int?,
    @SerializedName("trusted_hash") val twoFaHash: String?,
    @SerializedName("validation_sid") val validationSid: String?,
    @SerializedName("validation_type") val validationType: String?,
    @SerializedName("phone_mask") val phoneMask: String?,
    @SerializedName("redirect_uri") val redirectUrl: String?,
    @SerializedName("validation_resend") val validationResend: String?,
    @SerializedName("cant_get_code_open_restore") val isCanNotGetCodeNeedToOpenRestore: Boolean
) : Parcelable

@Parcelize
data class SendSmsResponse(
    @SerializedName("sid") val validationSid: String?,
    @SerializedName("delay") val delay: Int?,
    @SerializedName("validation_type") val validationType: String?,
    @SerializedName("validation_resend") val validationResend: String?
) : Parcelable
