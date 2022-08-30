package com.meloda.fast.data.videos

import okhttp3.MultipartBody

class VideosRepository(
    private val videosApi: VideosApi
) {

    suspend fun save() = videosApi.save()

    suspend fun upload(url: String, file: MultipartBody.Part) = videosApi.upload(url, file)

}