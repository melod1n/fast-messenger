package com.meloda.app.fast.auth.screens.login.model

sealed interface LoginError {

    data object WrongCredentials : LoginError
}
