package dev.meloda.fast.network.service.convos

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.ConvosDeleteResponse
import dev.meloda.fast.model.api.responses.ConvosGetByIdResponse
import dev.meloda.fast.model.api.responses.ConvosGetResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ConvosService {

    @FormUrlEncoded
    @POST(ConvosUrls.GET)
    suspend fun getConvos(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<ConvosGetResponse>, RestApiError>

    @FormUrlEncoded
    @POST(ConvosUrls.GET_BY_ID)
    suspend fun getConvosById(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<ConvosGetByIdResponse>, RestApiError>

    @FormUrlEncoded
    @POST(ConvosUrls.DELETE)
    suspend fun delete(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<ConvosDeleteResponse>, RestApiError>

    @FormUrlEncoded
    @POST(ConvosUrls.PIN)
    suspend fun pin(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(ConvosUrls.UNPIN)
    suspend fun unpin(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(ConvosUrls.REORDER_PINNED)
    suspend fun reorderPinned(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(ConvosUrls.ARCHIVE)
    suspend fun archive(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(ConvosUrls.UNARCHIVE)
    suspend fun unarchive(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>
}
