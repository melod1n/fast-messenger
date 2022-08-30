package com.meloda.fast.data.audios

import okhttp3.MultipartBody

class AudiosRepository(
    private val audiosApi: AudiosApi
) {

    suspend fun getUploadServer() = audiosApi.getUploadServer()

    suspend fun upload(url: String, file: MultipartBody.Part) = audiosApi.upload(url, file)

    suspend fun save(server: Int, audio: String, hash: String) = audiosApi.save(
        mapOf(
            "server" to server.toString(),
            "audio" to audio,
            "hash" to hash
        )
    )

}