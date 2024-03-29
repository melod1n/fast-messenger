package com.meloda.fast.data.photos

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.attachments.BaseVkPhoto
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.photos.PhotoUrls
import com.meloda.fast.api.network.photos.PhotosGetMessagesUploadServerResponse
import com.meloda.fast.api.network.photos.PhotosUploadPhotoResponse
import okhttp3.MultipartBody
import retrofit2.http.*

interface PhotosApi {

    @FormUrlEncoded
    @POST(PhotoUrls.GetMessagesUploadServer)
    suspend fun getUploadServer(
        @FieldMap map: Map<String, String>
    ): ApiAnswer<ApiResponse<PhotosGetMessagesUploadServerResponse>>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part photo: MultipartBody.Part
    ): ApiAnswer<PhotosUploadPhotoResponse>

    @FormUrlEncoded
    @POST(PhotoUrls.SaveMessagePhoto)
    suspend fun save(
        @FieldMap map: Map<String, String>
    ): ApiAnswer<ApiResponse<List<BaseVkPhoto>>>

}