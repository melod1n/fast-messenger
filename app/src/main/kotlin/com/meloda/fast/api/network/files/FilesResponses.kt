package com.meloda.fast.api.network.files

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.base.attachments.BaseVkFile
import com.meloda.fast.api.model.base.attachments.BaseVkVoiceMessage
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilesGetMessagesUploadServerResponse(
    @SerializedName("upload_url")
    val uploadUrl: String
) : Parcelable

@Parcelize
data class FilesUploadFileResponse(val file: String?, val error: String?) : Parcelable

@JsonClass(generateAdapter = true)
data class FilesSaveFileResponse(
    val type: String,
    @Json(name = "doc")
    val file: BaseVkFile?,
    @Json(name = "audio_message")
    val voiceMessage: BaseVkVoiceMessage?
)
