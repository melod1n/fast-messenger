package com.meloda.fast.api.network.audio

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.model.base.attachments.BaseVkAudio
import com.meloda.fast.api.network.ApiAnswer
import okhttp3.MultipartBody
import retrofit2.http.*

interface AudiosRepo {

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
    suspend fun save(@FieldMap map: Map<String, String>): ApiAnswer<ApiResponse<BaseVkAudio>>

}