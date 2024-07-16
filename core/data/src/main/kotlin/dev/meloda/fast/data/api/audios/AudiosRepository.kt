package dev.meloda.fast.data.api.audios

import dev.meloda.fast.model.api.responses.AudiosGetUploadServerResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import dev.meloda.fast.network.service.audios.AudiosService
import com.slack.eithernet.ApiResult
import okhttp3.MultipartBody

class AudiosRepository(
    private val audiosService: AudiosService
) {

    suspend fun getUploadServer(): ApiResult<ApiResponse<AudiosGetUploadServerResponse>, RestApiError> =
        audiosService.getUploadServer()

    suspend fun upload(url: String, file: MultipartBody.Part) = audiosService.upload(url, file)

    suspend fun save(server: Int, audio: String, hash: String) = audiosService.save(
        mapOf(
            "server" to server.toString(),
            "audio" to audio,
            "hash" to hash
        )
    )
}
