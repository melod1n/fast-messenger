package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.meloda.fast.api.model.attachments.VkVoiceMessage
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkVoiceMessage(
    val id: Int,
    val owner_id: Int,
    val duration: Int,
    val waveform: List<Int>,
    val link_ogg: String,
    val link_mp3: String,
    val access_key: String,
    val transcript_state: String,
    val transcript: String
) : Parcelable {

    fun asVkVoiceMessage() = VkVoiceMessage(
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