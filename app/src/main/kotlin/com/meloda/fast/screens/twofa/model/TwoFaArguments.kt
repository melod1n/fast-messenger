package com.meloda.fast.screens.twofa.model

import com.meloda.fast.model.base.UiText

data class TwoFaArguments(
    val validationSid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: TwoFaValidationType,
    val canResendSms: Boolean,
    val wrongCodeError: UiText?,
)
