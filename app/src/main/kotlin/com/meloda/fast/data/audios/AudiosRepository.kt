package com.meloda.fast.data.audios

import okhttp3.MultipartBody

class AudiosRepository(
    private val audiosService: AudiosService
) {

    suspend fun getUploadServer() = audiosService.getUploadServer()

    suspend fun upload(url: String, file: MultipartBody.Part) = audiosService.upload(url, file)

    suspend fun save(server: Int, audio: String, hash: String) = audiosService.save(
        mapOf(
            "server" to server.toString(),
            "audio" to audio,
            "hash" to hash
        )
    )

}
