package com.meloda.fast.screens.updates

sealed class UpdateState {
    object NewUpdate : UpdateState()
    object NoUpdates : UpdateState()
    object Loading : UpdateState()
    object Error : UpdateState()
    object Downloading : UpdateState()
}
