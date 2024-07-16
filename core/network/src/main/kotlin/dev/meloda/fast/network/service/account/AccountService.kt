package dev.meloda.fast.network.service.account

import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface AccountService {

    @GET(AccountUrls.SET_ONLINE)
    suspend fun setOnline(
        @QueryMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @GET(AccountUrls.SET_OFFLINE)
    suspend fun setOffline(
        @QueryMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>

    @FormUrlEncoded
    @POST(AccountUrls.REGISTER_DEVICE)
    suspend fun registerDevice(
        @FieldMap params: Map<String, String>
    ): ApiResult<ApiResponse<Int>, RestApiError>
}
