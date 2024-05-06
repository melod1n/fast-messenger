package com.meloda.app.fast.auth.screens.twofa.model

import com.meloda.app.fast.common.UiText

data class TwoFaScreenState(
    val twoFaSid: String,
    val twoFaCode: String?,
    val twoFaText: UiText,
    val canResendSms: Boolean,
    val codeError: String?,
    val delayTime: Int,
    val isNeedToOpenLogin: Boolean,
    val phoneMask: String
) {

    companion object {
        val EMPTY = TwoFaScreenState(
            twoFaSid = "",
            twoFaCode = null,
            twoFaText = UiText.Simple(""),
            canResendSms = false,
            codeError = null,
            delayTime = 0,
            isNeedToOpenLogin = false,
            phoneMask = ""
        )
    }
}
