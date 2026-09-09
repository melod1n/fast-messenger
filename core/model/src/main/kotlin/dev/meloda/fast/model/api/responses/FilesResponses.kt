package dev.meloda.fast.model.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import dev.meloda.fast.model.api.data.VkAudioMessageData
import dev.meloda.fast.model.api.data.VkFileData

@JsonClass(generateAdapter = true)
data class FilesGetMessagesUploadServerResponse(
    @Json(name = "upload_url")
    val uploadUrl: String
)

@JsonClass(generateAdapter = true)
data class FilesUploadFileResponse(
    val file: String? = null,
    val error: String? = null,
    @Json(name = "audio_message") val audioMessage: String? = null
)

@JsonClass(generateAdapter = true)
data class FilesSaveFileResponse(
    @Json(name = "type") val type: String? = null,
    @Json(name = "doc") val file: VkFileData? = null,
    @Json(name = "audio_message") val voiceMessage: VkAudioMessageData? = null
)
