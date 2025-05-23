package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkAudioMessageDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAudioMessageData(
    @Json(name = "id") val id: Long,
    @Json(name = "owner_id") val ownerId: Long,
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
