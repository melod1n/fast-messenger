package com.meloda.fast.api.network.audio

import okhttp3.MultipartBody
import javax.inject.Inject

class AudiosDataSource @Inject constructor(
    private val repo: AudiosRepo
) {

    suspend fun getUploadServer() = repo.getUploadServer()

    suspend fun upload(url: String, file: MultipartBody.Part) = repo.upload(url, file)

    suspend fun save(server: Int, audio: String, hash: String) = repo.save(
        mapOf(
            "server" to server.toString(),
            "audio" to audio,
            "hash" to hash
        )
    )

}