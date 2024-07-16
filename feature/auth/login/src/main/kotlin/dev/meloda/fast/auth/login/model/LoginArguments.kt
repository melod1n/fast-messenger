package com.meloda.fast.auth.login.model

data class LoginArguments(val code: String) {

    companion object {
        val EMPTY: LoginArguments = LoginArguments(code = "")
    }
}
