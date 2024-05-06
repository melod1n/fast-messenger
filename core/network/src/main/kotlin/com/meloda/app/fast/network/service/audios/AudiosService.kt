package com.meloda.app.fast.network.service.audios

import com.meloda.app.fast.model.api.data.VkAudioData
import com.meloda.app.fast.model.api.responses.AudiosGetUploadServerResponse
import com.meloda.app.fast.model.api.responses.AudiosUploadResponse
import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import okhttp3.MultipartBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface AudiosService {

    @POST(AudiosUrls.GET_UPLOAD_SERVER)
    suspend fun getUploadServer(): ApiResult<ApiResponse<AudiosGetUploadServerResponse>, RestApiError>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiResult<AudiosUploadResponse, RestApiError>

    @FormUrlEncoded
    @POST(AudiosUrls.SAVE)
    suspend fun save(@FieldMap map: Map<String, String>): ApiResult<ApiResponse<VkAudioData>, RestApiError>
}
