package com.meloda.app.fast.data

import com.meloda.app.fast.network.OAuthErrorDomain
import com.meloda.app.fast.network.RestApiErrorDomain
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

sealed class State<out T> {

    data object Idle : State<Nothing>()
    data class Success<T>(val data: T) : State<T>()
    data object Loading : State<Nothing>()

    sealed class Error : State<Nothing>() {

        data class ApiError(
            val errorCode: Int,
            val errorMessage: String,
        ) : Error()

        data object ConnectionError : Error()

        data object Unknown : Error()

        data object InternalError : Error()

        data class OAuthError(val error: OAuthErrorDomain) : Error()
    }

    fun isLoading(): Boolean = this is Loading

    companion object {

        val UNKNOWN_ERROR = Error.Unknown
    }
}

inline fun <T> State<T>.processState(
    error: (error: State.Error) -> (Unit),
    success: (data: T) -> (Unit),
    idle: (() -> (Unit)) = {},
    loading: (() -> (Unit)) = {},
) {
    when (this) {
        is State.Error -> error(this)
        State.Idle -> idle()
        State.Loading -> loading()
        is State.Success -> success(data)
    }
}

inline fun <T, R> Flow<State<T>>.mapSuccess(
    crossinline transform: suspend (value: T) -> R
): Flow<R> = filterIsInstance<State.Success<T>>()
    .map { state -> transform.invoke(state.data) }

fun RestApiErrorDomain?.toStateApiError(): State.Error = when (this) {
    null -> State.Error.ConnectionError
    else -> State.Error.ApiError(code, message)
}

fun OAuthErrorDomain?.toStateApiError(): State.Error = when (this) {
    null -> State.Error.ConnectionError
    else -> State.Error.OAuthError(this)
}

fun <T : Any> ApiResult<T, RestApiErrorDomain>.mapToState() = when (this) {
    is ApiResult.Success -> State.Success(this.value)

    is ApiResult.Failure.NetworkFailure -> State.Error.ConnectionError
    is ApiResult.Failure.UnknownFailure -> State.UNKNOWN_ERROR
    is ApiResult.Failure.HttpFailure -> this.error.toStateApiError()
    is ApiResult.Failure.ApiFailure -> this.error.toStateApiError()
}
