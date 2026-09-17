package dev.meloda.fast.model.api.data

import dev.meloda.fast.model.api.domain.VkAudioMessageDomain
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VkAudioMessageData(
    @Json(name = "id") val id: Long,
    @Json(name = "owner_id") val ownerId: Long,
    @Json(name = "duration") val duration: Int = 0,
    @Json(name = "waveform") val waveform: List<Int> = emptyList(),
    @Json(name = "link_ogg") val linkOgg: String? = null,
    @Json(name = "link_mp3") val linkMp3: String? = null,
    @Json(name = "access_key") val accessKey: String? = null,
    @Json(name = "transcript_state") val transcriptState: String? = null,
    @Json(name = "transcript") val transcript: String? = null
) {

    fun toDomain() = VkAudioMessageDomain(
        id = id,
        ownerId = ownerId,
        duration = duration,
        waveform = waveform,
        linkOgg = linkOgg.orEmpty(),
        linkMp3 = linkMp3.orEmpty(),
        accessKey = accessKey.orEmpty(),
        transcriptState = transcriptState,
        transcript = transcript
    )
}
