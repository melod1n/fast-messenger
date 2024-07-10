package com.meloda.app.fast.model.api.data

import com.meloda.app.fast.model.api.domain.VkAudioMessageDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAudioMessageData(
    @Json(name = "id") val id: Int,
    @Json(name = "owner_id") val ownerId: Int,
    @Json(name = "duration") val duration: Int,
    @Json(name = "waveform") val waveform: List<Int>,
    @Json(name = "link_ogg") val linkOgg: String,
    @Json(name = "link_mp3") val linkMp3: String,
    @Json(name = "access_key") val accessKey: String,
    @Json(name = "transcript_state") val transcriptState: String?,
    @Json(name = "transcript") val transcript: String?
) {

    fun toDomain() = VkAudioMessageDomain(
        id = id,
        ownerId = ownerId,
        duration = duration,
        waveform = waveform,
        linkOgg = linkOgg,
        linkMp3 = linkMp3,
        accessKey = accessKey,
        transcriptState = transcriptState,
        transcript = transcript
    )
}
