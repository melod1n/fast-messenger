package com.meloda.fast.base

import com.slack.eithernet.ApiResult

data class RestApiErrorDomain(
    val code: Int,
    val message: String
)

fun RestApiErrorDomain?.toStateApiError(): State.Error = when (this) {
    null -> State.Error.ConnectionError
    else -> State.Error.ApiError(code, message)
}

fun ApiResult.Failure.HttpFailure<*>?.tryCastToRestErrorDomain() =
    this?.error as? RestApiErrorDomain

fun ApiResult.Failure.ApiFailure<*>?.tryCastToRestErrorDomain() =
    this?.error as? RestApiErrorDomain
