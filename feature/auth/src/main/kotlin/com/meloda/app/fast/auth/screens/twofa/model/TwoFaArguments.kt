package com.meloda.app.fast.auth.screens.twofa.model

data class TwoFaArguments(
    val validationSid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: String,
    val canResendSms: Boolean,
    val wrongCodeError: String?,
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
