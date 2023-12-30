package com.meloda.fast.screens.main.model

sealed class LongPollState {
    data object ForegroundService : LongPollState()
    data object DefaultService : LongPollState()
    data object Stop : LongPollState()
}
