package com.meloda.fast.data.audios

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.data.VkAudioData
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.audio.AudiosGetUploadServerResponse
import com.meloda.fast.api.network.audio.AudiosUploadResponse
import com.meloda.fast.api.network.audio.AudiosUrls
import okhttp3.MultipartBody
import retrofit2.http.*

interface AudiosApi {

    @POST(AudiosUrls.GetUploadServer)
    suspend fun getUploadServer(): ApiAnswer<ApiResponse<AudiosGetUploadServerResponse>>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiAnswer<AudiosUploadResponse>

    @FormUrlEncoded
    @POST(AudiosUrls.Save)
    suspend fun save(@FieldMap map: Map<String, String>): ApiAnswer<ApiResponse<VkAudioData>>

}
