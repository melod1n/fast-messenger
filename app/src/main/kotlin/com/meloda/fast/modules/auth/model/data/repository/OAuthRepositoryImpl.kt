package com.meloda.fast.modules.auth.model.data.repository

import com.meloda.fast.api.OAuthAnswer
import com.meloda.fast.api.mapResult
import com.meloda.fast.api.network.BaseOAuthError
import com.meloda.fast.api.network.oauth.AuthDirectRequest
import com.meloda.fast.api.network.oauth.AuthDirectResponse
import com.meloda.fast.modules.auth.model.data.service.OAuthService
import com.meloda.fast.modules.auth.model.domain.repository.OAuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthRepositoryImpl(
    private val oAuthService: OAuthService
) : OAuthRepository {

    override suspend fun auth(
        params: AuthDirectRequest
    ): OAuthAnswer<AuthDirectResponse, BaseOAuthError> =
        withContext(Dispatchers.IO) {
            oAuthService.auth(params.map).mapResult(
                successMapper = { response ->
                    response
                },
                errorMapper = { error ->
                    error
                }
            )
        }
}
