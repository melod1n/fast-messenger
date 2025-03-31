package dev.meloda.fast.network.service.oauth

import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import dev.meloda.fast.model.api.responses.AuthDirectErrorOnlyResponse
import dev.meloda.fast.model.api.responses.AuthDirectResponse
import dev.meloda.fast.model.api.responses.GetSilentTokenResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface OAuthService {

    @DecodeErrorBody
    @GET(OAuthUrls.GET_SILENT_TOKEN)
    suspend fun auth(
        @QueryMap param: Map<String, String>
    ): ApiResult<AuthDirectResponse, AuthDirectErrorOnlyResponse>

    @DecodeErrorBody
    @GET(OAuthUrls.GET_SILENT_TOKEN)
    suspend fun getSilentToken(
        @QueryMap param: Map<String, String>
    ): ApiResult<GetSilentTokenResponse, AuthDirectErrorOnlyResponse>
}
