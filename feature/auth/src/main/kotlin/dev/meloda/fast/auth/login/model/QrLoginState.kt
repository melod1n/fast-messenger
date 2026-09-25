package dev.meloda.fast.auth.login.model

sealed interface QrLoginState {
    data object Scanning : QrLoginState
    data object Processing : QrLoginState
    data object Authorized : QrLoginState
    data class Error(val messageResId: Int) : QrLoginState
}
