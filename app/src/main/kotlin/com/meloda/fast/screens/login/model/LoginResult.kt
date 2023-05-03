package com.meloda.fast.screens.login.model

sealed class LoginResult {
    object Authorized : LoginResult()
    object Cancelled : LoginResult()
}
