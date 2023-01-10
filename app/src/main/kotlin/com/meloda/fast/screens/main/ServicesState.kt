package com.meloda.fast.screens.main

sealed class ServicesState {
    object Started : ServicesState()
    object Stopped : ServicesState()
    object Unknown : ServicesState()
}
