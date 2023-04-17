package com.meloda.fast.screens.twofa.model

import com.meloda.fast.model.base.UiText

data class TwoFaScreenState(
    val twoFaSid: String,
    val twoFaCode: String,
    val twoFaText: UiText,
    val canResendSms: Boolean,
) {

    companion object {
        val EMPTY = TwoFaScreenState(
            twoFaSid = "",
            twoFaCode = "",
            twoFaText = UiText.Simple(""),
            canResendSms = false,
        )
    }
}
