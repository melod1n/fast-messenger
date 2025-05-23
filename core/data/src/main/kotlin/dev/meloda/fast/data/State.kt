package dev.meloda.fast.data

import com.slack.eithernet.ApiResult
import dev.meloda.fast.network.OAuthErrorDomain
import dev.meloda.fast.network.RestApiErrorDomain
import dev.meloda.fast.network.VkErrorCode

sealed class State<out T> {

    data object Idle : State<Nothing>()
    data class Success<T>(val data: T) : State<T>()
    data object Loading : State<Nothing>()

    sealed class Error : State<Nothing>() {

        data class ApiError(
            val errorCode: VkErrorCode,
            val errorMessage: String,
        ) : Error()

        data object ConnectionError : Error()

        data object UnknownError : Error()

        data object InternalError : Error()

        data class OAuthError(val error: OAuthErrorDomain) : Error()
    }

    fun isLoading(): Boolean = this is Loading

    companion object {

        val UNKNOWN_ERROR = Error.UnknownError
    }
}

inline fun <T> State<T>.processState(
    error: (error: State.Error) -> Unit,
    success: (data: T) -> Unit,
    idle: (() -> (Unit)) = {},
    loading: (() -> (Unit)) = {},
    any: () -> Unit = {}
) {
    when (this) {
        is State.Error -> {
            any()
            error(this)
        }

        State.Idle -> idle()

        State.Loading -> loading()

        is State.Success -> {
            any()
            success(data)
        }
    }
}

fun OAuthErrorDomain?.toStateApiError(): State.Error {
    if (this == null) return State.Error.ConnectionError
    return State.Error.OAuthError(this)
}

fun RestApiErrorDomain?.toStateApiError(): State.Error = when (this) {
    null -> State.Error.ConnectionError
    else -> State.Error.ApiError(VkErrorCode.parse(code), message)
}

fun <T : Any> ApiResult<T, OAuthErrorDomain>.asState() = when (this) {
    is ApiResult.Success -> State.Success(this.value)

    is ApiResult.Failure.NetworkFailure -> State.Error.ConnectionError
    is ApiResult.Failure.UnknownFailure -> State.UNKNOWN_ERROR
    is ApiResult.Failure.HttpFailure -> this.error.toStateApiError()
    is ApiResult.Failure.ApiFailure -> this.error.toStateApiError()
}

fun <T : Any, N> ApiResult<T, OAuthErrorDomain>.asState(successMapper: (T) -> N) =
    when (this) {
        is ApiResult.Success -> State.Success(successMapper(this.value))

        is ApiResult.Failure.NetworkFailure -> State.Error.ConnectionError
        is ApiResult.Failure.UnknownFailure -> State.UNKNOWN_ERROR
        is ApiResult.Failure.HttpFailure -> this.error.toStateApiError()
        is ApiResult.Failure.ApiFailure -> this.error.toStateApiError()
    }

fun <T : Any, E : Any> ApiResult<T, E>.success(): T =
    when (this) {
        is ApiResult.Success -> value
        else -> throw IllegalArgumentException()
    }

fun <T : Any> ApiResult<T, RestApiErrorDomain>.mapToState() = when (this) {
    is ApiResult.Success -> State.Success(this.value)

    is ApiResult.Failure.NetworkFailure -> State.Error.ConnectionError
    is ApiResult.Failure.UnknownFailure -> State.UNKNOWN_ERROR
    is ApiResult.Failure.HttpFailure -> this.error.toStateApiError()
    is ApiResult.Failure.ApiFailure -> this.error.toStateApiError()
}

fun <T : Any, N> ApiResult<T, RestApiErrorDomain>.mapToState(successMapper: (T) -> N) =
    when (this) {
        is ApiResult.Success -> State.Success(successMapper(this.value))

        is ApiResult.Failure.NetworkFailure -> State.Error.ConnectionError
        is ApiResult.Failure.UnknownFailure -> State.UNKNOWN_ERROR
        is ApiResult.Failure.HttpFailure -> this.error.toStateApiError()
        is ApiResult.Failure.ApiFailure -> this.error.toStateApiError()
    }
