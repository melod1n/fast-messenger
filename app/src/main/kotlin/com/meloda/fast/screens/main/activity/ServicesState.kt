package com.meloda.fast.screens.main.activity

sealed class ServicesState {
    object Started : ServicesState()
    object Stopped : ServicesState()
    object Unknown : ServicesState()
}
