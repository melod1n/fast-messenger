package dev.meloda.fast.photoviewer.model

sealed class PhotoViewEffect {
    data class Navigate(val intent: PhotoViewNavigationIntent) : PhotoViewEffect()
}
