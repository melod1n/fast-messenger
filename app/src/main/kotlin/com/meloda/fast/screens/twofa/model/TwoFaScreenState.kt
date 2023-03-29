package com.meloda.fast.screens.twofa.model

data class TwoFaScreenState(
    val twoFaCode: String
) {

    companion object {
        val EMPTY = TwoFaScreenState(
            twoFaCode = ""
        )
    }
}
