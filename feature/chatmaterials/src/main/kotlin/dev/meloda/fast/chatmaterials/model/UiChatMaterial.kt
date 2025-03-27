package dev.meloda.fast.chatmaterials.model

sealed class UiChatMaterial(
    open val conversationMessageId: Int
) {

    data class Photo(
        override val conversationMessageId: Int,
        val previewUrl: String
    ) : UiChatMaterial(conversationMessageId)

    data class Video(
        override val conversationMessageId: Int,
        val previewUrl: String?,
        val title: String,
        val views: Int,
        val duration: String
    ) : UiChatMaterial(conversationMessageId)

    data class Audio(
        override val conversationMessageId: Int,
        val previewUrl: String?,
        val title: String,
        val artist: String,
        val duration: String
    ) : UiChatMaterial(conversationMessageId)

    data class File(
        override val conversationMessageId: Int,
        val previewUrl: String?,
        val title: String,
        val size: String,
        val extension: String
    ) : UiChatMaterial(conversationMessageId)

    data class Link(
        override val conversationMessageId: Int,
        val previewUrl: String?,
        val title: String?,
        val url: String,
        val urlFirstChar: String
    ) : UiChatMaterial(conversationMessageId)
}
