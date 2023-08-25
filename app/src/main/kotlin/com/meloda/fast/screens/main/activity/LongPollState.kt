package com.meloda.fast.screens.main.activity

sealed class LongPollState {
    data object ForegroundService : LongPollState()
    data object DefaultService : LongPollState()
    data object Stop : LongPollState()
}
