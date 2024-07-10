package com.meloda.app.fast.network

import com.slack.eithernet.ApiResult

fun <Success : Any, Error : Any, SuccessDomain : Any, ErrorDomain : Any>
        ApiResult<Success, Error>.mapResult(
    successMapper: (Success) -> SuccessDomain,
    errorMapper: (Error?) -> ErrorDomain?
): ApiResult<SuccessDomain, ErrorDomain> {
    if (BuildConfig.DEBUG) printStackTraceIfAny()

    return when (this) {
        is ApiResult.Success -> {
            ApiResult.success(successMapper(value))
        }

        is ApiResult.Failure.NetworkFailure -> {
            ApiResult.networkFailure(error)
        }

        is ApiResult.Failure.UnknownFailure -> {
            ApiResult.unknownFailure(error)
        }

        is ApiResult.Failure.HttpFailure -> {
            ApiResult.httpFailure(code, errorMapper(error))
        }

        is ApiResult.Failure.ApiFailure -> {
            ApiResult.apiFailure(errorMapper(error))
        }
    }
}

fun <Success : Any, Error : Any, SuccessDomain : Any, ErrorDomain : Any>
        ApiResult<Success, Error>.mapOAuthResult(
    successMapper: (Success) -> SuccessDomain,
    errorMapper: (Error?) -> ErrorDomain?
): ApiResult<SuccessDomain, ErrorDomain> {
    if (BuildConfig.DEBUG) printStackTraceIfAny()

    return when (this) {
        is ApiResult.Success -> {
            ApiResult.success(successMapper(value))
        }

        is ApiResult.Failure.NetworkFailure -> {
            ApiResult.networkFailure(error)
        }

        is ApiResult.Failure.UnknownFailure -> {
            ApiResult.unknownFailure(error)
        }

        is ApiResult.Failure.HttpFailure -> {

            ApiResult.httpFailure(code, errorMapper(error))
        }

        is ApiResult.Failure.ApiFailure -> {
            ApiResult.apiFailure(errorMapper(error))
        }
    }
}

fun <Success : ApiResponse<*>, SuccessDomain : Any, ErrorDomain : Any>
        ApiResult<Success, RestApiError>.mapApiResult(
    successMapper: (Success) -> SuccessDomain,
    errorMapper: (RestApiError?) -> ErrorDomain?
): ApiResult<SuccessDomain, ErrorDomain> {
    if (BuildConfig.DEBUG) printStackTraceIfAny()

    return when (this) {
        is ApiResult.Success -> {
            if (value.isSuccessful) {
                ApiResult.success(successMapper(value))
            } else {
                ApiResult.apiFailure(errorMapper(value.error))
            }
        }

        is ApiResult.Failure.NetworkFailure -> ApiResult.networkFailure(error)
        is ApiResult.Failure.UnknownFailure -> ApiResult.unknownFailure(error)
        is ApiResult.Failure.HttpFailure -> ApiResult.httpFailure(code, errorMapper(error))
        is ApiResult.Failure.ApiFailure -> ApiResult.apiFailure(errorMapper(error))
    }
}

fun <T : Any, R : ApiResponse<T>> ApiResult<R, RestApiError>.mapApiDefault(): ApiResult<T, RestApiErrorDomain> =
    mapResult(
        successMapper = { response -> response.requireResponse() },
        errorMapper = { error -> error?.toDomain() }
    )

//@OptIn(ExperimentalContracts::class)
//inline fun <R : Any, E : Any, C> OAuthResponse<R, E>.fold(
//    onSuccess: (value: R) -> C,
//    onFailure: (failure: E) -> C,
//): C {
//    contract {
//        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
//        callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE)
//    }
//    return when (this) {
//        is OAuthResponse.Success -> onSuccess(response)
//        is OAuthResponse.Error -> onFailure(error)
//    }
//}

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

fun ApiResult.Failure.HttpFailure<*>?.tryCastToRestErrorDomain() =
    this?.error as? RestApiErrorDomain

fun ApiResult.Failure.ApiFailure<*>?.tryCastToRestErrorDomain() =
    this?.error as? RestApiErrorDomain
