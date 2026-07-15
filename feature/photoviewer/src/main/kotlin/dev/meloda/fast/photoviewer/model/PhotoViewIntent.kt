package dev.meloda.fast.photoviewer.model

sealed class PhotoViewIntent {
    data object Back: PhotoViewIntent()

    data class PageChange(val newPage: Int) : PhotoViewIntent()

    data object ShareClick : PhotoViewIntent()
    data object OpenInClick : PhotoViewIntent()
    data object CopyLinkClick : PhotoViewIntent()
    data object CopyClick : PhotoViewIntent()

}
