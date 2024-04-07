package com.meloda.fast.data.files

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.base.RestApiError
import com.meloda.fast.api.network.files.FilesGetMessagesUploadServerResponse
import com.meloda.fast.api.network.files.FilesSaveFileResponse
import com.meloda.fast.api.network.files.FilesUploadFileResponse
import com.meloda.fast.api.network.files.FilesUrls
import com.slack.eithernet.ApiResult
import okhttp3.MultipartBody
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface FilesApi {

    @FormUrlEncoded
    @POST(FilesUrls.GetMessagesUploadServer)
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
    @POST(FilesUrls.Save)
    suspend fun save(
        @FieldMap map: Map<String, String>
    ): ApiResult<ApiResponse<FilesSaveFileResponse>, RestApiError>

}
