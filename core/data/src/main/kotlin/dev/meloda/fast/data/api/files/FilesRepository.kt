package dev.meloda.fast.data.api.files

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.FilesGetMessagesUploadServerResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import dev.meloda.fast.network.service.files.FilesService
import okhttp3.MultipartBody

class FilesRepository(
    private val filesService: FilesService
) {

    enum class FileType(val value: String) {
        FILE("doc"),
        AUDIO_MESSAGE("audio_message")
    }

    suspend fun getMessagesUploadServer(
        peerId: Long,
        type: FileType
    ): ApiResult<ApiResponse<FilesGetMessagesUploadServerResponse>, RestApiError> =
        filesService.getUploadServer(
            mapOf(
                "peer_id" to peerId.toString(),
                "type" to type.value
            )
        )

    suspend fun uploadFile(url: String, file: MultipartBody.Part) = filesService.upload(url, file)

    suspend fun saveMessageFile(file: String) = filesService.save(mapOf("file" to file))

}
