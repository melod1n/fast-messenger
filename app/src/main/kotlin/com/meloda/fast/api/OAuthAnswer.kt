package com.meloda.fast.api

import com.meloda.fast.api.network.BaseOAuthError

sealed interface OAuthAnswer<out R, out E : BaseOAuthError> {

    data class Success<out R>(val response: R) : OAuthAnswer<R, Nothing>

    data class Error<out E : BaseOAuthError>(val error: E) : OAuthAnswer<Nothing, E>
}
