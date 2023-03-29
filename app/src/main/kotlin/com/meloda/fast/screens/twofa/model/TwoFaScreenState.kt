package com.meloda.fast.screens.twofa.model

data class TwoFaScreenState(
    val twoFaSid: String,
    val twoFaCode: String
) {

    companion object {
        val EMPTY = TwoFaScreenState(
            twoFaSid = "",
            twoFaCode = ""
        )
    }
}
