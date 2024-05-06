package com.meloda.app.fast.auth.screens.login.model

data class LoginArguments(val code: String) {

    companion object {
        val EMPTY: LoginArguments = LoginArguments(code = "")
    }
}
