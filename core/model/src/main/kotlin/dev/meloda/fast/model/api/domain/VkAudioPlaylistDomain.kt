package dev.meloda.fast.model.api.domain

import dev.meloda.fast.model.api.data.AttachmentType

data class VkAudioPlaylistDomain(
    val id: Long,
    val ownerId: Long,
    val title: String,
    val description: String,
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.AUDIO_PLAYLIST
}
