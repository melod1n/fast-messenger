package com.meloda.fast.auth.login.model

sealed interface LoginError {

    data object WrongCredentials : LoginError
}
