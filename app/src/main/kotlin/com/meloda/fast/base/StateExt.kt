package com.meloda.fast.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map

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
