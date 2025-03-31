package dev.meloda.fast.network.service.auth

import com.slack.eithernet.ApiResult
import dev.meloda.fast.model.api.responses.ExchangeSilentTokenResponse
import dev.meloda.fast.model.api.responses.GetAnonymTokenResponse
import dev.meloda.fast.model.api.responses.GetExchangeTokenResponse
import dev.meloda.fast.model.api.responses.ValidateLoginResponse
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface AuthService {

    @GET(AuthUrls.VALIDATE_PHONE)
    suspend fun validatePhone(
        @Query("sid") validationSid: String
    ): ApiResult<ApiResponse<ValidatePhoneResponse>, RestApiError>

    @GET(AuthUrls.VALIDATE_LOGIN)
    suspend fun validateLogin(
        @QueryMap param: Map<String, String>
    ): ApiResult<ApiResponse<ValidateLoginResponse>, RestApiError>

    @FormUrlEncoded
    @POST(AuthUrls.GET_ANONYM_TOKEN)
//    @Headers("User-Agent: VKDesktop/1.0.0-dev.0")
    suspend fun getAnonymToken(
        @FieldMap param: Map<String, String>
    ): ApiResult<ApiResponse<GetAnonymTokenResponse>, RestApiError>

    @FormUrlEncoded
    @POST(AuthUrls.EXCHANGE_SILENT_TOKEN)
//    @Headers("User-Agent: VKDesktop/1.0.0-dev.0")
    suspend fun exchangeSilentToken(
        @FieldMap param: Map<String, String>
    ): ApiResult<ApiResponse<ExchangeSilentTokenResponse>, RestApiError>

    @FormUrlEncoded
    @POST(AuthUrls.GET_EXCHANGE_TOKEN)
//    @Headers("User-Agent: VKDesktop/1.0.0-dev.0")
    suspend fun getExchangeToken(
        @FieldMap param: Map<String, String>
    ): ApiResult<ApiResponse<GetExchangeTokenResponse>, RestApiError>
}
