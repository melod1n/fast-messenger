package com.meloda.fast.base

import com.meloda.fast.BuildConfig
import com.slack.eithernet.ApiResult

fun <Success : Any, Error : Any, SuccessDomain : Any, ErrorDomain : Any>
        ApiResult<Success, Error>.mapResult(
    successMapper: (Success) -> SuccessDomain,
    errorMapper: (Error?) -> ErrorDomain?
): ApiResult<SuccessDomain, ErrorDomain> {
    if (BuildConfig.DEBUG) printStackTraceIfAny()

    return when (this) {
        is ApiResult.Success -> ApiResult.success(successMapper(value))
        is ApiResult.Failure.NetworkFailure -> ApiResult.networkFailure(error)
        is ApiResult.Failure.UnknownFailure -> ApiResult.unknownFailure(error)
        is ApiResult.Failure.HttpFailure -> ApiResult.httpFailure(code, errorMapper(error))
        is ApiResult.Failure.ApiFailure -> ApiResult.apiFailure(errorMapper(error))
    }
}

fun <Success : Any, Error : Any> ApiResult<Success, Error>.isSuccess(): Boolean =
    this is ApiResult.Success

fun <Success : Any, Error : Any> ApiResult<Success, Error>.printStackTraceIfAny() {
    val throwable = when (this) {
        is ApiResult.Failure.NetworkFailure -> error
        is ApiResult.Failure.UnknownFailure -> error
        else -> null
    }
    throwable?.printStackTrace()
}
