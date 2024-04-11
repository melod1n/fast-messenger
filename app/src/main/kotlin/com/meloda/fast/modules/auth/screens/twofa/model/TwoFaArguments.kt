package com.meloda.fast.modules.auth.screens.twofa.model

import com.meloda.fast.model.base.UiText

data class TwoFaArguments(
    val validationSid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: String,
    val canResendSms: Boolean,
    val wrongCodeError: UiText?,
) {

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
