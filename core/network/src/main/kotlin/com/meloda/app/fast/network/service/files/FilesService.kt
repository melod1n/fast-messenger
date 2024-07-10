package com.meloda.app.fast.network.service.files

import com.meloda.app.fast.model.api.responses.FilesGetMessagesUploadServerResponse
import com.meloda.app.fast.model.api.responses.FilesSaveFileResponse
import com.meloda.app.fast.model.api.responses.FilesUploadFileResponse
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

interface FilesService {

    @FormUrlEncoded
    @POST(FilesUrls.GET_MESSAGES_UPLOAD_SERVER)
    suspend fun getUploadServer(
        @FieldMap map: Map<String, String>
    ): ApiResult<ApiResponse<FilesGetMessagesUploadServerResponse>, RestApiError>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): ApiResult<FilesUploadFileResponse, RestApiError>

    @FormUrlEncoded
    @POST(FilesUrls.SAVE)
    suspend fun save(
        @FieldMap map: Map<String, String>
    ): ApiResult<ApiResponse<FilesSaveFileResponse>, RestApiError>

}
