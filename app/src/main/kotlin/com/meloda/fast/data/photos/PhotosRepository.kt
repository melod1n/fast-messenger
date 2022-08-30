package com.meloda.fast.data.photos

import com.meloda.fast.api.network.photos.PhotosSaveMessagePhotoRequest
import okhttp3.MultipartBody

class PhotosRepository(
    private val photosApi: PhotosApi
) {

    suspend fun getMessagesUploadServer(peerId: Int) =
        photosApi.getUploadServer(mapOf("peer_id" to peerId.toString()))

    suspend fun uploadPhoto(url: String, photo: MultipartBody.Part) = photosApi.upload(url, photo)

    suspend fun saveMessagePhoto(body: PhotosSaveMessagePhotoRequest) =
        photosApi.save(body.map)

}