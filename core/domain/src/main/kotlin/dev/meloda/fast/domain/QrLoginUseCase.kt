package dev.meloda.fast.domain

import com.slack.eithernet.ApiResult
import dev.meloda.fast.common.VkConstants
import dev.meloda.fast.data.api.auth.AuthRepository
import dev.meloda.fast.model.api.responses.GetAuthCodeStatusResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class QrLoginUseCase(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(authCode: String): QrLoginResult {
        val anonymousTokenResponse = runCatchingCancellable {
            authRepository.getAnonymToken(
                clientId = VkConstants.VK_APP_ID,
                clientSecret = VkConstants.VK_SECRET
            )
        }.getOrElse { return QrLoginResult.Failed }
        val anonymousToken = when (anonymousTokenResponse) {
            is ApiResult.Success -> anonymousTokenResponse.value.token.takeIf(String::isNotBlank)
                ?: return QrLoginResult.Failed
            is ApiResult.Failure.NetworkFailure -> return QrLoginResult.ConnectionError
            else -> return QrLoginResult.Failed
        }

        val sessionResponse = runCatchingCancellable {
            authRepository.setAuthCodeStatus(
                accessToken = anonymousToken,
                authCode = authCode
            )
        }.getOrElse { return QrLoginResult.Failed }
        val session = when (sessionResponse) {
            is ApiResult.Success -> sessionResponse.value
            is ApiResult.Failure.NetworkFailure -> return QrLoginResult.ConnectionError
            else -> return QrLoginResult.Failed
        }

        val pollingDelayMs = session.pollingDelay
            ?.takeIf { it > 0 }
            ?.toLong()
            ?.times(MILLIS_IN_SECOND)
            ?: return QrLoginResult.Failed
        val expiresAtMs = session.expiresIn
            ?.toLong()
            ?.times(MILLIS_IN_SECOND)
            ?: return QrLoginResult.Failed

        while (System.currentTimeMillis() <= expiresAtMs) {
            val responseResult = runCatchingCancellable {
                authRepository.getAuthCodeStatus(
                    accessToken = anonymousToken,
                    authCode = authCode
                )
            }.getOrElse { return QrLoginResult.Failed }
            val response = when (responseResult) {
                is ApiResult.Success -> responseResult.value
                is ApiResult.Failure.NetworkFailure -> return QrLoginResult.ConnectionError
                else -> return QrLoginResult.Failed
            }

            when (response.status.toQrStatus()) {
                QrStatus.Created, QrStatus.Opened -> delay(pollingDelayMs.milliseconds)
                QrStatus.Authorized -> return response.toAuthorizedResult()
                QrStatus.Declined -> return QrLoginResult.Declined
                QrStatus.Expired -> return QrLoginResult.Expired
                QrStatus.Unknown, null -> return QrLoginResult.Failed
            }
        }

        return QrLoginResult.Expired
    }

    private inline fun <T> runCatchingCancellable(block: () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private fun GetAuthCodeStatusResponse.toAuthorizedResult(): QrLoginResult {
        val token = accessToken?.takeIf(String::isNotBlank) ?: return QrLoginResult.Failed
        val id = userId ?: return QrLoginResult.Failed
        return QrLoginResult.Authorized(accessToken = token, userId = id)
    }

    private fun Int?.toQrStatus(): QrStatus? = when (this) {
        null -> null
        0 -> QrStatus.Created
        1 -> QrStatus.Opened
        2 -> QrStatus.Authorized
        3 -> QrStatus.Declined
        4 -> QrStatus.Expired
        else -> QrStatus.Unknown
    }

    private enum class QrStatus {
        Created,
        Opened,
        Authorized,
        Declined,
        Expired,
        Unknown
    }

    private companion object {
        const val MILLIS_IN_SECOND = 1_000L
    }
}

sealed interface QrLoginResult {
    data class Authorized(
        val accessToken: String,
        val userId: Long
    ) : QrLoginResult

    data object Declined : QrLoginResult
    data object Expired : QrLoginResult
    data object ConnectionError : QrLoginResult
    data object Failed : QrLoginResult
}
