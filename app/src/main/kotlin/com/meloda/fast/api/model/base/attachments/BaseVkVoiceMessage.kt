package com.meloda.fast.api.model.base.attachments

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BaseVkVoiceMessage(
    val id: Int,
    @SerializedName("owner_id")
    val ownerId: Int,
    val duration: Int,
    val waveform: List<Int>,
    @SerializedName("link_ogg")
    val linkOgg: String,
    @SerializedName("link_mp3")
    val linkMp3: String,
    @SerializedName("access_key")
    val accessKey: String,
    @SerializedName("transcript_state")
    val transcriptState: String,
    val transcript: String
) : Parcelable