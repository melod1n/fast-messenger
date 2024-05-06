package com.meloda.app.fast.network

sealed interface OAuthResponse<out R, out E : BaseOAuthError> {

    data class Success<out R>(val response: R) : OAuthResponse<R, Nothing>

    data class Error<out E : BaseOAuthError>(val error: E) : OAuthResponse<Nothing, E>
}
