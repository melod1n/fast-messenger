package com.meloda.fast.data.photos

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.model.data.VkPhotoData
import com.meloda.fast.api.network.photos.PhotoUrls
import com.meloda.fast.api.network.photos.PhotosGetMessagesUploadServerResponse
import com.meloda.fast.api.network.photos.PhotosUploadPhotoResponse
import com.slack.eithernet.ApiResult
import okhttp3.MultipartBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface PhotosService {

    @FormUrlEncoded
    @POST(PhotoUrls.GetMessagesUploadServer)
    suspend fun getUploadServer(
        @FieldMap map: Map<String, String>
    ): ApiResult<ApiResponse<PhotosGetMessagesUploadServerResponse>, RestApiError>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part photo: MultipartBody.Part
    ): ApiResult<PhotosUploadPhotoResponse, RestApiError>

    @FormUrlEncoded
    @POST(PhotoUrls.SaveMessagePhoto)
    suspend fun save(
        @FieldMap map: Map<String, String>
    ): ApiResult<ApiResponse<List<VkPhotoData>>, RestApiError>
}
