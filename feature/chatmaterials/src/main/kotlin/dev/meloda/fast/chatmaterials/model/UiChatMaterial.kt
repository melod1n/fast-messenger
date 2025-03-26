package dev.meloda.fast.chatmaterials.model

sealed class UiChatMaterial {

    data class Photo(
        val previewUrl: String
    ) : UiChatMaterial()

    data class Video(
        val previewUrl: String?,
        val title: String,
        val views: Int,
        val duration: String
    ) : UiChatMaterial()

    data class Audio(
        val previewUrl: String?,
        val title: String,
        val artist: String,
        val duration: String
    ) : UiChatMaterial()

    data class File(
        val previewUrl: String?,
        val title: String,
        val size: String,
        val extension: String
    ) : UiChatMaterial()

    data class Link(
        val previewUrl: String?,
        val title: String?,
        val url: String,
        val urlFirstChar: String
    ) : UiChatMaterial()
}
