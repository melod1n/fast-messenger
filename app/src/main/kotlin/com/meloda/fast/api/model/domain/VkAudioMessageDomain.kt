package com.meloda.fast.api.model.domain

import com.meloda.fast.R
import com.meloda.fast.api.model.data.AttachmentType
import com.meloda.fast.model.base.UiText

data class VkAudioMessageDomain(
    val id: Int,
    val ownerId: Int,
    val duration: Int,
    val waveform: List<Int>,
    val linkOgg: String,
    val linkMp3: String,
    val accessKey: String,
    val transcriptState: String?,
    val transcript: String?
) : VkAttachment {

    override val type: AttachmentType = AttachmentType.AUDIO_MESSAGE

    val className: String = this::class.java.name

    override fun getUiText(): UiText =
        UiText.Resource(R.string.message_attachments_audio_message)
}
