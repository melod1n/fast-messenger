package com.meloda.fast.api.network.files

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meloda.fast.api.model.base.attachments.BaseVkFile
import com.meloda.fast.api.model.base.attachments.BaseVkVoiceMessage
import kotlinx.parcelize.Parcelize

@Parcelize
data class FilesGetMessagesUploadServerResponse(
    @SerializedName("upload_url")
    val uploadUrl: String
) : Parcelable

@Parcelize
data class FilesUploadFileResponse(val file: String?, val error: String?) : Parcelable

@Parcelize
data class FilesSaveFileResponse(
    val type: String,
    @SerializedName("doc")
    val file: BaseVkFile?,
    @SerializedName("audio_message")
    val voiceMessage: BaseVkVoiceMessage?
) : Parcelable