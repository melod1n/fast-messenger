package com.meloda.app.fast.data.api.oauth

import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.network.BaseOAuthError
import com.meloda.app.fast.network.OAuthResponse

interface OAuthRepository {

    suspend fun auth(
        params: AuthDirectRequest
    ): OAuthResponse<AuthDirectResponse, BaseOAuthError>
}
