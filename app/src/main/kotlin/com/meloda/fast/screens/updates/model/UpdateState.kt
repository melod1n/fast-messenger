package com.meloda.fast.screens.updates.model

sealed class UpdateState {
    object NewUpdate : UpdateState()
    object NoUpdates : UpdateState()
    object Loading : UpdateState()
    object Error : UpdateState()
    object Downloading : UpdateState()
    object Downloaded : UpdateState()

    fun isNewUpdate() = this == NewUpdate
    fun isLoading() = this == Loading
    fun isDownloading() = this == Downloading
    fun isDownloaded() = this == Downloaded
}
