package dev.meloda.fast.network.service.auth

import dev.meloda.fast.model.api.responses.ValidateLoginResponse
import dev.meloda.fast.model.api.responses.ValidatePhoneResponse
import dev.meloda.fast.network.ApiResponse
import dev.meloda.fast.network.RestApiError
import com.slack.eithernet.ApiResult
import retrofit2.http.GET
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
}
