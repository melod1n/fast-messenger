package dev.meloda.fast.network.service.oauth

import dev.meloda.fast.model.api.responses.AuthDirectResponse
import dev.meloda.fast.model.api.responses.GetAnonymousTokenResponse
import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface OAuthService {

    @DecodeErrorBody
    @GET(OAuthUrls.DIRECT_AUTH)
    suspend fun auth(
        @QueryMap param: Map<String, String>
    ): ApiResult<AuthDirectResponse, AuthDirectResponse>

    @DecodeErrorBody
    @GET(OAuthUrls.GET_ANONYMOUS_TOKEN)
    suspend fun getAnonymousToken(
        @QueryMap param: Map<String, String>
    ): ApiResult<GetAnonymousTokenResponse, GetAnonymousTokenResponse>
}
