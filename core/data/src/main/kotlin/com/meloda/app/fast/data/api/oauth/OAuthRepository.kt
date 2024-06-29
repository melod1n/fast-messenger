package com.meloda.app.fast.data.api.oauth

import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.network.OAuthError
import com.meloda.app.fast.network.OAuthErrorDomain
import com.meloda.app.fast.network.OAuthResponse
import com.slack.eithernet.ApiResult

interface OAuthRepository {

    suspend fun auth(
        params: AuthDirectRequest
    ): ApiResult<AuthDirectResponse, OAuthErrorDomain>
}
