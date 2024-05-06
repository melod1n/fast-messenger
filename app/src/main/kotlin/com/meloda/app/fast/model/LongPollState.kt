package com.meloda.app.fast.model

sealed class LongPollState {
    data object ForegroundService : LongPollState()
    data object DefaultService : LongPollState()
    data object Stop : LongPollState()
}
