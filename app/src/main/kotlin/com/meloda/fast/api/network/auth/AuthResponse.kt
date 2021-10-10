package com.meloda.fast.api.network.auth

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResponseAuthDirect(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("user_id") val userId: Int? = null,
    @SerializedName("trusted_hash") val twoFaHash: String? = null,
    @SerializedName("validation_sid") val validationSid: String? = null
) : Parcelable

@Parcelize
data class ResponseSendSms(
    @SerializedName("sid") val validationSid: String?,
    @SerializedName("delay") val delay: Int?,
    @SerializedName("validation_type") val validationType: String?,
    @SerializedName("validation_resend") val validationResend: String?
) : Parcelable