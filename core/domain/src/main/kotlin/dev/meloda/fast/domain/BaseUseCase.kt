package dev.meloda.fast.domain

import dev.meloda.fast.data.State
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow

interface BaseUseCase {

    suspend fun <T> FlowCollector<State<T>>.emitState(stateBlock: suspend () -> State<T>) {
        emit(State.Loading)
        emit(stateBlock())
    }

    fun <T> flowNewState(stateBlock: suspend () -> State<T>) =
        flow { emitState(stateBlock) }
}
