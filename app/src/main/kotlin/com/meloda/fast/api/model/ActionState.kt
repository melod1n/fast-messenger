package com.meloda.fast.api.model

sealed class ActionState {
    data object Phantom : ActionState()
    data object CallInProgress : ActionState()
    data object None : ActionState()

    companion object {
        fun parse(isPhantom: Boolean, isCallInProgress: Boolean): ActionState {
            return when {
                isPhantom -> Phantom
                isCallInProgress -> CallInProgress
                else -> None
            }
        }
    }
}
