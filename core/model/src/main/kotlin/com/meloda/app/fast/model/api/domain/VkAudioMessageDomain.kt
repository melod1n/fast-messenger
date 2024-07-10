package com.meloda.app.fast.model.api.domain

import com.meloda.app.fast.model.api.data.AttachmentType

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
}
