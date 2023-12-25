package com.meloda.fast.screens.twofa.model

import android.os.Parcelable
import com.meloda.fast.model.base.UiText
import kotlinx.parcelize.Parcelize

@Parcelize
data class TwoFaArguments(
    val validationSid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: String,
    val canResendSms: Boolean,
    val wrongCodeError: UiText?,
) : Parcelable {

    companion object {
        val EMPTY: TwoFaArguments = TwoFaArguments(
            validationSid = "",
            redirectUri = "",
            phoneMask = "",
            validationType = TwoFaValidationType.Sms.value,
            canResendSms = false,
            wrongCodeError = null
        )
    }
}
