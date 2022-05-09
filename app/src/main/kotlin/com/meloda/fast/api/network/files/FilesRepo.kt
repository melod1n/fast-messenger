package com.meloda.fast.api.network.files

import com.meloda.fast.api.base.ApiResponse
import com.meloda.fast.api.network.Answer
import okhttp3.MultipartBody
import retrofit2.http.*

interface FilesRepo {

    @FormUrlEncoded
    @POST(FilesUrls.GetMessagesUploadServer)
    suspend fun getUploadServer(
        @FieldMap map: Map<String, String>
    ): Answer<ApiResponse<FilesGetMessagesUploadServerResponse>>

    @Multipart
    @POST
    suspend fun upload(
        @Url url: String,
        @Part file: MultipartBody.Part
    ): Answer<FilesUploadFileResponse>

    @FormUrlEncoded
    @POST(FilesUrls.Save)
    suspend fun save(
        @FieldMap map: Map<String, String>
    ): Answer<ApiResponse<FilesSaveFileResponse>>

}