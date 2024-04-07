package com.meloda.fast.data.videos

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.network.videos.VideosSaveResponse
import com.meloda.fast.api.network.videos.VideosUploadResponse
import com.meloda.fast.api.network.videos.VideosUrls
import com.slack.eithernet.ApiResult
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface VideosApi {

    @POST(VideosUrls.Save)
    suspend fun save(): ApiResult<ApiResponse<VideosSaveResponse>, RestApiError>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiResult<VideosUploadResponse, RestApiError>
}
