package com.meloda.app.fast.network.service.oauth

import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.network.BaseOAuthError
import com.meloda.app.fast.network.OAuthResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface OAuthService {

    @GET(OAuthUrls.DIRECT_AUTH)
    suspend fun auth(@QueryMap param: Map<String, String?>): OAuthResponse<AuthDirectResponse, BaseOAuthError>
}
