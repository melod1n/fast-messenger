package com.meloda.fast.api.network.videos

import okhttp3.MultipartBody
import javax.inject.Inject

class VideosDataSource @Inject constructor(
    private val repo: VideosRepo
) {

    suspend fun save() = repo.save()

    suspend fun upload(url: String, file: MultipartBody.Part) = repo.upload(url, file)

}