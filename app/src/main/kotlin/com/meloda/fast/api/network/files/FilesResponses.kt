package com.meloda.fast.api.network.files

import com.meloda.fast.api.model.data.VkFileData
import com.meloda.fast.api.model.data.VkAudioMessageData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class FilesGetMessagesUploadServerResponse(
    @Json(name = "upload_url")
    val uploadUrl: String
)

data class FilesUploadFileResponse(val file: String?, val error: String?)

@JsonClass(generateAdapter = true)
data class FilesSaveFileResponse(
    @Json(name = "type") val type: String,
    @Json(name = "doc") val file: VkFileData?,
    @Json(name = "audio_message") val voiceMessage: VkAudioMessageData?
)
