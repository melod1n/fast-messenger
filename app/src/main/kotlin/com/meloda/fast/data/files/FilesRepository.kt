package com.meloda.fast.data.files

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody

class FilesRepository(
    private val filesApi: FilesApi
) {

    enum class FileType(val value: String) {
        @SerializedName("doc")
        File("doc"),

        @SerializedName("audio_message")
        VoiceMessage("audio_message")
    }

    suspend fun getMessagesUploadServer(peerId: Int, type: FileType) =
        filesApi.getUploadServer(
            mapOf(
                "peer_id" to peerId.toString(),
                "type" to type.value
            )
        )

    suspend fun uploadFile(url: String, file: MultipartBody.Part) = filesApi.upload(url, file)

    suspend fun saveMessageFile(file: String) = filesApi.save(mapOf("file" to file))

}