package com.meloda.fast.modules.auth.screens.twofa.model

import com.meloda.fast.model.base.UiText

data class TwoFaScreenState(
    val twoFaSid: String,
    val twoFaCode: String?,
    val twoFaText: UiText,
    val canResendSms: Boolean,
    val codeError: UiText?,
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
