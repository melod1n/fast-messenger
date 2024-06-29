package com.meloda.app.fast.data.api.oauth

import com.meloda.app.fast.model.api.requests.AuthDirectRequest
import com.meloda.app.fast.model.api.responses.AuthDirectResponse
import com.meloda.app.fast.network.OAuthErrorDomain
import com.meloda.app.fast.network.service.oauth.OAuthService
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OAuthRepositoryImpl(
    private val oAuthService: OAuthService
) : OAuthRepository {

    override suspend fun auth(
        params: AuthDirectRequest
    ): ApiResult<AuthDirectResponse, OAuthErrorDomain> =
        withContext(Dispatchers.IO) {
            oAuthService.auth(params.map)
             ApiResult.success(
                 AuthDirectResponse(
                     accessToken = null,
                     userId = null,
                     twoFaHash = null,
                     validationSid = null,
                     validationType = null,
                     phoneMask = null,
                     redirectUrl = null,
                     validationResend = null,
                     restoreIfCannotGetCode = null
                 )
             )
//                .mapResult(
//                successMapper = { response ->
//                    response
//                },
//                errorMapper = { error ->
//                    error?.toDomain()
//                }
//            )
        }
}
