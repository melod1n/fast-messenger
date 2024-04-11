package com.meloda.fast.api.model

enum class ActionState {
    PHANTOM, CALL_IN_PROGRESS, NONE;

    // TODO: 11/04/2024, Danil Nikolaev: implement
    fun getResourceId(): Int {
        return -1
    }

    companion object {
        fun parse(isPhantom: Boolean, isCallInProgress: Boolean): ActionState {
            return when {
                isPhantom -> PHANTOM
                isCallInProgress -> CALL_IN_PROGRESS
                else -> NONE
            }
        }
    }
}
