package com.meloda.fast.api.network.photos

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.attachments.BaseVkPhoto
import com.meloda.fast.api.network.Answer
import okhttp3.MultipartBody
import retrofit2.http.*

interface PhotosRepo {

    @FormUrlEncoded
    @POST(PhotoUrls.GetMessagesUploadServer)
    suspend fun getUploadServer(
        @FieldMap map: Map<String, String>
    ): Answer<ApiResponse<PhotosGetMessagesUploadServerResponse>>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part photo: MultipartBody.Part
    ): Answer<PhotosUploadPhotoResponse>

    @FormUrlEncoded
    @POST(PhotoUrls.SaveMessagePhoto)
    suspend fun save(
        @FieldMap map: Map<String, String>
    ): Answer<ApiResponse<List<BaseVkPhoto>>>

}