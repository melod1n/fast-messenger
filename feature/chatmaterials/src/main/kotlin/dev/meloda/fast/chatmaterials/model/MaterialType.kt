package dev.meloda.fast.chatmaterials.model

enum class MaterialType {
    PHOTO, VIDEO, AUDIO, FILE, LINK;

    override fun toString(): String = when (this) {
        PHOTO -> "photo"
        VIDEO -> "video"
        AUDIO -> "audio"
        FILE -> "doc"
        LINK -> "link"
    }
}
