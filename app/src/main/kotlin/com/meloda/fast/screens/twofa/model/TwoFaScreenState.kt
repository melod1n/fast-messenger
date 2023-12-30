package com.meloda.fast.screens.twofa.model

import com.meloda.fast.model.base.UiText

data class TwoFaScreenState(
    val twoFaSid: String,
    val twoFaCode: String,
    val twoFaText: UiText,
    val canResendSms: Boolean,
    val codeError: UiText?,
    val delayTime: Int,
    val isNeedToOpenLogin: Boolean,
) {

    companion object {
        val EMPTY = TwoFaScreenState(
            twoFaSid = "",
            twoFaCode = "",
            twoFaText = UiText.Simple(""),
            canResendSms = false,
            codeError = null,
            delayTime = 0,
            isNeedToOpenLogin = false,
        )
    }
}
