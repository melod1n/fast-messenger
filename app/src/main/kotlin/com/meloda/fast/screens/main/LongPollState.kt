package com.meloda.fast.screens.main

sealed class LongPollState {
    object ForegroundService : LongPollState()
    object DefaultService : LongPollState()
    object Stop : LongPollState()
}
