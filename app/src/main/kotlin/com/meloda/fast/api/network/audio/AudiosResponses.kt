package com.meloda.fast.api.network.audio

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudiosGetUploadServerResponse(
    @SerializedName("upload_url")
    val uploadUrl: String
) : Parcelable

@Parcelize
data class AudiosUploadResponse(
    val redirect: String,
    val server: Int,
    val audio: String?,
    val hash: String,
    val error: String?
) : Parcelable