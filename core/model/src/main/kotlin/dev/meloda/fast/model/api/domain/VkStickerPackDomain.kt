package dev.meloda.fast.model.api.domain

data class VkStickerPackDomain(
    val id: Long,
    val title: String,
    val stickerIds: List<Long>
) {

    val previewStickerId: Long?
        get() = stickerIds.firstOrNull()
}
