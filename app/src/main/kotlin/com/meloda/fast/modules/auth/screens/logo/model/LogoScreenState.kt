package com.meloda.fast.modules.auth.screens.logo.model

data class LogoScreenState(
    val isNeedToRestart: Boolean
) {

    companion object {

        val EMPTY: LogoScreenState = LogoScreenState(
            isNeedToRestart = false
        )
    }
}
