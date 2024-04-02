package com.meloda.fast.data.audios

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.model.data.VkAudioData
import com.meloda.fast.api.network.audio.AudiosGetUploadServerResponse
import com.meloda.fast.api.network.audio.AudiosUploadResponse
import com.meloda.fast.api.network.audio.AudiosUrls
import com.slack.eithernet.ApiResult
import okhttp3.MultipartBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface AudiosApi {

    @POST(AudiosUrls.GetUploadServer)
    suspend fun getUploadServer(): ApiResult<ApiResponse<AudiosGetUploadServerResponse>, RestApiError>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiResult<AudiosUploadResponse, RestApiError>

    @FormUrlEncoded
    @POST(AudiosUrls.Save)
    suspend fun save(@FieldMap map: Map<String, String>): ApiResult<ApiResponse<VkAudioData>, RestApiError>

}
