package dev.meloda.fast.data.api.files

import dev.meloda.fast.network.service.files.FilesService
import okhttp3.MultipartBody

class FilesRepository(
    private val filesService: FilesService
) {

    // TODO: 05/05/2024, Danil Nikolaev: reimplement
//    enum class FileType(val value: String) {
//        @Json(name = "doc")
//        FILE("doc"),
//
//        @Json(name = "audio_message")
//        AUDIO_MESSAGE("audio_message")
//    }
//
//    suspend fun getMessagesUploadServer(peerId: Int, type: FileType) =
//        filesService.getUploadServer(
//            mapOf(
//                "peer_id" to peerId.toString(),
//                "type" to type.value
//            )
//        )

    suspend fun uploadFile(url: String, file: MultipartBody.Part) = filesService.upload(url, file)

    suspend fun saveMessageFile(file: String) = filesService.save(mapOf("file" to file))

}
