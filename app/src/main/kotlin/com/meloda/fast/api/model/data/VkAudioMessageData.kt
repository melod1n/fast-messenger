package com.meloda.fast.api.model.data

import com.meloda.fast.api.model.domain.VkAudioMessageDomain
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAudioMessageData(
    val id: Int,
    val owner_id: Int,
    val duration: Int,
    val waveform: List<Int>,
    val link_ogg: String,
    val link_mp3: String,
    val access_key: String,
    val transcript_state: String?,
    val transcript: String?
) {

    fun toDomain() = VkAudioMessageDomain(
        id = id,
        ownerId = owner_id,
        duration = duration,
        waveform = waveform,
        linkOgg = link_ogg,
        linkMp3 = link_mp3,
        accessKey = access_key,
        transcriptState = transcript_state,
        transcript = transcript
    )
}
