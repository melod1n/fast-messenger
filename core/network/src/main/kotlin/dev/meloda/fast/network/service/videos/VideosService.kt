package dev.meloda.fast.network.service.videos

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.VideosGetResponse
import dev.meloda.fast.model.api.responses.VideosSaveResponse
import dev.meloda.fast.model.api.responses.VideosUploadResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Url

interface VideosService {

    @POST(VideosUrls.SAVE)
    suspend fun save(
        @Query("is_video_message") isVideoMessage: Int? = null,
        @Query("wallpost") wallpost: Int = 0,
        @Query("name") name: String? = null
    ): ApiResult<ApiResponse<VideosSaveResponse>, RestApiError>

    @POST(VideosUrls.GET)
    suspend fun get(
        @Query("videos") videos: String
    ): ApiResult<ApiResponse<VideosGetResponse>, RestApiError>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiResult<VideosUploadResponse, RestApiError>
}
