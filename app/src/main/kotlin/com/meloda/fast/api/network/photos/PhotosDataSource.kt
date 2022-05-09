package com.meloda.fast.api.network.photos

import okhttp3.MultipartBody
import javax.inject.Inject

class PhotosDataSource @Inject constructor(
    private val repo: PhotosRepo
) {

    suspend fun getMessagesUploadServer(peerId: Int) =
        repo.getUploadServer(mapOf("peer_id" to peerId.toString()))

    suspend fun uploadPhoto(url: String, photo: MultipartBody.Part) = repo.upload(url, photo)

    suspend fun saveMessagePhoto(body: PhotosSaveMessagePhotoRequest) =
        repo.save(body.map)

}