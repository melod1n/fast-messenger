package dev.meloda.fast.common

import dev.meloda.fast.common.model.LongPollState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface LongPollController {
    val currentState: StateFlow<LongPollState>
    val stateToApply: StateFlow<LongPollState>

    fun updateCurrentState(newState: LongPollState)
    fun setStateToApply(newState: LongPollState)
}

class LongPollControllerImpl : LongPollController {

    override val currentState = MutableStateFlow<LongPollState>(LongPollState.Stopped)
    override val stateToApply = MutableStateFlow<LongPollState>(LongPollState.Stopped)

    override fun updateCurrentState(newState: LongPollState) {
        currentState.value = newState
    }

    override fun setStateToApply(newState: LongPollState) {
        currentState.value = newState
    }
}
