package dev.meloda.fast.data.api.photos

import dev.meloda.fast.model.api.requests.PhotosSaveMessagePhotoRequest
import dev.meloda.fast.network.service.photos.PhotosService
import okhttp3.MultipartBody

class PhotosRepository(
    private val photosService: PhotosService
) {

    suspend fun getMessagesUploadServer(peerId: Int) =
        photosService.getUploadServer(mapOf("peer_id" to peerId.toString()))

    suspend fun uploadPhoto(url: String, photo: MultipartBody.Part) =
        photosService.upload(url, photo)

    suspend fun saveMessagePhoto(body: PhotosSaveMessagePhotoRequest) =
        photosService.save(body.map)
}
