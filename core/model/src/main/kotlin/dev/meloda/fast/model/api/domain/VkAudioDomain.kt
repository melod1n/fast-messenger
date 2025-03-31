package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkAudioDomain(
    val id: Long,
    val ownerId: Long,
    val title: String,
    val artist: String,
    val url: String,
    val duration: Int,
    val accessKey: String?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.AUDIO

    override fun toString(): String {
        val result = StringBuilder(type.value).append(ownerId).append('_').append(id)
        if (!accessKey.isNullOrBlank()) {
            result.append('_')
            result.append(accessKey)
        }
        return result.toString()
    }
}
