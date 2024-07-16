package dev.meloda.fast.network.service.photos

import dev.meloda.fast.model.api.data.VkPhotoData
import dev.meloda.fast.model.api.responses.PhotosGetMessagesUploadServerResponse
import dev.meloda.fast.model.api.responses.PhotosUploadPhotoResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
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
    @POST(PhotoUrls.GET_MESSAGES_UPLOAD_SERVER)
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
    @POST(PhotoUrls.SAVE_MESSAGE_PHOTO)
    suspend fun save(
        @FieldMap map: Map<String, String>
    ): ApiResult<ApiResponse<List<VkPhotoData>>, RestApiError>
}
