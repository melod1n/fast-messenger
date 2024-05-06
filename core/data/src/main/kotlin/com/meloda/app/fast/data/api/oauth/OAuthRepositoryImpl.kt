package com.meloda.app.fast.data.api.oauth

import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.network.BaseOAuthError
import com.meloda.app.fast.network.OAuthResponse
import com.meloda.app.fast.network.mapResult
import com.meloda.app.fast.network.service.oauth.OAuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthRepositoryImpl(
    private val oAuthService: OAuthService
) : OAuthRepository {

    override suspend fun auth(
        params: AuthDirectRequest
    ): OAuthResponse<AuthDirectResponse, BaseOAuthError> =
        withContext(Dispatchers.IO) {
            oAuthService.auth(params.map).mapResult(
                successMapper = { response -> response },
                errorMapper = { error -> error }
            )
        }
}
