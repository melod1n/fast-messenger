package com.meloda.fast.modules.auth.screens.login.model

sealed interface LoginError {

    data object WrongCredentials : LoginError
}
