package com.meloda.fast.data.videos

import okhttp3.MultipartBody

class VideosRepository(
    private val videosService: VideosService
) {

    suspend fun save() = videosService.save()

    suspend fun upload(url: String, file: MultipartBody.Part) = videosService.upload(url, file)
}
