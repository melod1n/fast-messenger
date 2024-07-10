package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

data class VkAudioPlaylistDomain(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val description: String,
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.AUDIO_PLAYLIST
}
