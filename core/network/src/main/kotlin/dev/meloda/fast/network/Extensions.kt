package dev.meloda.fast.network

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

fun <R : Any> ApiResult<R, RestApiError>.mapDefault(): ApiResult<R, RestApiErrorDomain> =
    mapResult(
        successMapper = { response -> response },
        errorMapper = { error -> error?.toDomain() }
    )

fun <T : Any, R : ApiResponse<T>> ApiResult<R, RestApiError>.mapApiDefault(): ApiResult<T, RestApiErrorDomain> =
    mapResult(
        successMapper = { response -> response.requireResponse() },
        errorMapper = { error -> error?.toDomain() }
    )

fun <Success : Any, Error : Any> ApiResult<Success, Error>.printStackTraceIfAny() {
    val throwable = when (this) {
        is ApiResult.Failure.NetworkFailure -> error
        is ApiResult.Failure.UnknownFailure -> error
        else -> null
    }
    throwable?.printStackTrace()
}
