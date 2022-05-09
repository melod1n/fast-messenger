package com.meloda.fast.api.network.files

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import javax.inject.Inject

class FilesDataSource @Inject constructor(
    private val repo: FilesRepo
) {
    enum class FileType(val value: String) {
        @SerializedName("doc")
        File("doc"),

        @SerializedName("audio_message")
        VoiceMessage("audio_message")
    }

    suspend fun getMessagesUploadServer(peerId: Int, type: FileType) =
        repo.getUploadServer(
            mapOf(
                "peer_id" to peerId.toString(),
                "type" to type.value
            )
        )

    suspend fun uploadFile(url: String, file: MultipartBody.Part) = repo.upload(url, file)

    suspend fun saveMessageFile(file: String) = repo.save(mapOf("file" to file))

}