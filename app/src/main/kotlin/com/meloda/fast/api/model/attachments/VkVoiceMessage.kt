package com.meloda.fast.api.model.attachments

import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class VkVoiceMessage(
    val id: Int,
    val ownerId: Int,
    val duration: Int,
    val waveform: List<Int>,
    val linkOgg: String,
    val linkMp3: String,
    val accessKey: String,
    val transcriptState: String?,
    val transcript: String?
) : VkAttachment() {

    @IgnoredOnParcel
    val className: String = this::class.java.name

}
