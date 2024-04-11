package com.meloda.fast.modules.auth.model.data.service

import com.meloda.fast.api.OAuthAnswer
import com.meloda.fast.api.network.BaseOAuthError
import com.meloda.fast.api.network.oauth.AuthDirectResponse
import com.meloda.fast.api.network.oauth.OAuthUrls
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface OAuthService {

    @GET(OAuthUrls.DIRECT_AUTH)
    suspend fun auth(@QueryMap param: Map<String, String?>): OAuthAnswer<AuthDirectResponse, BaseOAuthError>
}
