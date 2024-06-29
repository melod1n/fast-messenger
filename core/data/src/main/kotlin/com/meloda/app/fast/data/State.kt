package com.meloda.app.fast.data

import com.meloda.app.fast.network.OAuthErrorDomain
import com.meloda.app.fast.network.RestApiErrorDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

/** Возможные состояния для одного потока данных в репозитории */
sealed class State<out T> {

    /**
     * Репозиторий еще ничего не делал
     */
    data object Idle : State<Nothing>()

    /**
     * Получение данных репозиторием закончилось успехом
     */
    data class Success<T>(val data: T) : State<T>()

    /**
     * Репозиторий в процессе получения данных
     */
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
