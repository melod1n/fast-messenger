package com.meloda.fast.modules.auth.model.domain.repository

import com.meloda.fast.api.OAuthAnswer
import com.meloda.fast.api.network.BaseOAuthError
import com.meloda.fast.api.network.oauth.AuthDirectRequest
import com.meloda.fast.api.network.oauth.AuthDirectResponse

interface OAuthRepository {

    suspend fun auth(
        params: AuthDirectRequest
    ): OAuthAnswer<AuthDirectResponse, BaseOAuthError>
}
