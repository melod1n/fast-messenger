package com.meloda.app.fast.network.service.oauth

import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.slack.eithernet.ApiResult
import com.slack.eithernet.DecodeErrorBody
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface OAuthService {

    @DecodeErrorBody
    @GET(OAuthUrls.DIRECT_AUTH)
    suspend fun auth(
        @QueryMap param: Map<String, String?>
    ): ApiResult<AuthDirectResponse, AuthDirectResponse>
}
