package com.meloda.fast.screens.main.activity

sealed class LongPollState {
    object ForegroundService : LongPollState()
    object DefaultService : LongPollState()
    object Stop : LongPollState()
}
