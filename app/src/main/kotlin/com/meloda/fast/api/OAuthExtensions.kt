package com.meloda.fast.api

import com.meloda.fast.api.network.BaseOAuthError
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <Response : Any, ResponseMapped : Any, Error : BaseOAuthError, ErrorMapped : BaseOAuthError> OAuthAnswer<Response, Error>.mapResult(
    successMapper: (Response) -> ResponseMapped,
    errorMapper: (Error) -> ErrorMapped
): OAuthAnswer<ResponseMapped, ErrorMapped> {
    return when (this) {
        is OAuthAnswer.Success -> {
            OAuthAnswer.Success(successMapper(this.response))
        }

        is OAuthAnswer.Error -> {
            OAuthAnswer.Error(errorMapper(this.error))
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R : Any, E : BaseOAuthError, C> OAuthAnswer<R, E>.fold(
    onSuccess: (value: R) -> C,
    onFailure: (failure: E) -> C,
): C {
    contract {
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is OAuthAnswer.Success -> onSuccess(response)
        is OAuthAnswer.Error -> onFailure(error)
    }
}
