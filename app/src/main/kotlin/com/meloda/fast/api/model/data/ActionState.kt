package com.meloda.fast.api.model.data

sealed class ActionState {
    object Phantom : ActionState()
    object CallInProgress : ActionState()
    object None : ActionState()

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
