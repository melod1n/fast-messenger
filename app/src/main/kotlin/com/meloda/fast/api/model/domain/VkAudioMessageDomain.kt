package com.meloda.fast.api.model.domain

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

    val className: String = this::class.java.name
}
