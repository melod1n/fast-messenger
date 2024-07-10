package com.meloda.app.fast.data.api.videos

import com.meloda.app.fast.network.service.videos.VideosService
import okhttp3.MultipartBody

class VideosRepository(
    private val videosService: VideosService
) {

    suspend fun save() = videosService.save()

    // TODO: 05/05/2024, Danil Nikolaev: research, maybe remove multipart.body
    suspend fun upload(url: String, file: MultipartBody.Part) = videosService.upload(url, file)
}
