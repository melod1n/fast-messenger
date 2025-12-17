package dev.meloda.fast.chatmaterials.model

sealed class UiChatMaterial(
    open val cmId: Long
) {

    data class Photo(
        override val cmId: Long,
        val previewUrl: String
    ) : UiChatMaterial(cmId)

    data class Video(
        override val cmId: Long,
        val previewUrl: String?,
        val title: String,
        val views: Int,
        val duration: String
    ) : UiChatMaterial(cmId)

    data class Audio(
        override val cmId: Long,
        val previewUrl: String?,
        val title: String,
        val artist: String,
        val duration: String
    ) : UiChatMaterial(cmId)

    data class File(
        override val cmId: Long,
        val previewUrl: String?,
        val title: String,
        val size: String,
        val extension: String
    ) : UiChatMaterial(cmId)

    data class Link(
        override val cmId: Long,
        val previewUrl: String?,
        val title: String?,
        val url: String,
        val urlFirstChar: String
    ) : UiChatMaterial(cmId)
}
