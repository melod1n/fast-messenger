package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.network.*
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.notNull

abstract class BaseViewModel : ViewModel() {

    open suspend fun sendSingleEvent(event: VkEvent) {}

    suspend fun <T> sendRequestNotNull(
        onError: ErrorHandler? = null,
        request: suspend () -> ApiAnswer<T>
    ): T = sendRequest(onError, request).notNull()

    suspend fun <T> sendRequest(
        onError: ErrorHandler? = null,
        request: suspend () -> ApiAnswer<T>,
    ): T? {
        return when (val response = request()) {
            is ApiAnswer.Success -> response.data
            is ApiAnswer.Error -> {
                val error = response.error

                if (!onError?.handleError(error).isTrue) {
                    checkErrors(error)
                }

                null
            }
        }
    }

    protected suspend fun checkErrors(throwable: Throwable) {
        when (throwable) {
            is TokenExpiredError -> {
                sendSingleEvent(TokenExpiredErrorEvent)
            }
            is AuthorizationError -> {
                sendSingleEvent(AuthorizationErrorEvent)
            }
            is UserBannedError -> {
                throwable.banInfo.let { banInfo ->
                    sendSingleEvent(
                        UserBannedEvent(
                            memberName = banInfo.memberName,
                            message = banInfo.message,
                            restoreUrl = banInfo.restoreUrl,
                            accessToken = banInfo.accessToken
                        )
                    )
                }
            }
            is ValidationRequiredError -> {
                sendSingleEvent(
                    ValidationRequiredEvent(
                        sid = throwable.validationSid,
                        redirectUri = throwable.redirectUri,
                        phoneMask = throwable.phoneMask,
                        validationType = throwable.validationType,
                        canResendSms = throwable.validationResend == "sms",
                        codeError = null
                    )
                )
            }
            is CaptchaRequiredError -> {
                sendSingleEvent(
                    CaptchaRequiredEvent(
                        sid = throwable.captchaSid,
                        image = throwable.captchaImg
                    )
                )
            }

            is ApiError -> {
                sendSingleEvent(
                    if (throwable.errorMessage == null) {
                        UnknownErrorEvent
                    } else {
                        ErrorTextEvent(errorText = requireNotNull(throwable.errorMessage))
                    }
                )
            }
            else -> {
                sendSingleEvent(
                    if (throwable.message == null) {
                        UnknownErrorEvent
                    } else {
                        ErrorTextEvent(requireNotNull(throwable.message))
                    }
                )
            }
        }
    }
}
