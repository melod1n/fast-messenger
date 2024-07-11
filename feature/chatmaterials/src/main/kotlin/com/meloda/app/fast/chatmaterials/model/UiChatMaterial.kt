package com.meloda.app.fast.chatmaterials.model

sealed class UiChatMaterial {

    data class Photo(
        val previewUrl: String
    ) : UiChatMaterial()

    data class Video(
        val previewUrl: String
    ) : UiChatMaterial()

    data class Audio(
        val previewUrl: String?,
        val title: String,
        val artist: String,
        val duration: String
    ) : UiChatMaterial()

    data class File(
        val title: String
    ) : UiChatMaterial()

    data class Link(
        val title: String,
        val previewUrl: String?
    ) : UiChatMaterial()
}
