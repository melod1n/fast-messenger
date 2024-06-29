package com.meloda.app.fast.auth.screens.twofa.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class TwoFaArguments(
    val validationSid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: String,
    val canResendSms: Boolean,
    val wrongCodeError: String?,
) : Parcelable {

    companion object {
        val EMPTY: TwoFaArguments = TwoFaArguments(
            validationSid = "",
            redirectUri = "",
            phoneMask = "",
            validationType = TwoFaValidationType.Sms.value,
            canResendSms = false,
            wrongCodeError = null,
        )
    }
}
