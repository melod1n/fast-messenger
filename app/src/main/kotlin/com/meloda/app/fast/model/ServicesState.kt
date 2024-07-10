package com.meloda.app.fast.model

sealed class ServicesState {
    data object Started : ServicesState()
    data object Stopped : ServicesState()
    data object Unknown : ServicesState()
}
