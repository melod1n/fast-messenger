package com.meloda.app.fast.data.api.audios

import com.meloda.app.fast.model.api.responses.AudiosGetUploadServerResponse
import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
import com.meloda.app.fast.network.service.audios.AudiosService
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
