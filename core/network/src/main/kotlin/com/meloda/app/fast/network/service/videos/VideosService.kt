package com.meloda.app.fast.network.service.videos

import com.meloda.app.fast.model.api.responses.VideosSaveResponse
import com.meloda.app.fast.model.api.responses.VideosUploadResponse
import com.meloda.app.fast.network.ApiResponse
import com.meloda.app.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface VideosService {

    @POST(VideosUrls.SAVE)
    suspend fun save(): ApiResult<ApiResponse<VideosSaveResponse>, RestApiError>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiResult<VideosUploadResponse, RestApiError>
}
