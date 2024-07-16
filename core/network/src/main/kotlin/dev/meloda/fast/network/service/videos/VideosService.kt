package dev.meloda.fast.network.service.videos

import dev.meloda.fast.model.api.responses.VideosSaveResponse
import dev.meloda.fast.model.api.responses.VideosUploadResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
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
