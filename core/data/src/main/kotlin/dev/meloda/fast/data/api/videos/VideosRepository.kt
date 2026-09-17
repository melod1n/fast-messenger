package dev.meloda.fast.data.api.videos

import dev.meloda.fast.network.service.videos.VideosService
import okhttp3.MultipartBody

class VideosRepository(
    private val videosService: VideosService
) {

    suspend fun save(
        isVideoMessage: Boolean = false,
        name: String? = null
    ) = videosService.save(
        isVideoMessage = if (isVideoMessage) 1 else null,
        name = name
    )

    suspend fun get(videos: String) = videosService.get(videos = videos)

    suspend fun upload(url: String, file: MultipartBody.Part) = videosService.upload(url, file)
}
