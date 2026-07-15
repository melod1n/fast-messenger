package dev.meloda.fast.photoviewer.model

sealed class PhotoViewNavigationIntent {
    data object Back : PhotoViewNavigationIntent()
}
