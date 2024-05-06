package com.meloda.app.fast.auth.screens.logo.model

data class LogoScreenState(
    val isNeedToRestart: Boolean
) {

    companion object {

        val EMPTY: LogoScreenState = LogoScreenState(
            isNeedToRestart = false
        )
    }
}
