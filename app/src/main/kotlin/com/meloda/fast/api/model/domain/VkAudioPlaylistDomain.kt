package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkAudioPlaylistDomain(
    val id: Int,
    val ownerId: Int,
    val title: String,
    val description: String,
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.AUDIO_PLAYLIST

    override fun getUiText(): UiText = UiText.Resource(R.string.message_attachments_audio_playlist)
}
