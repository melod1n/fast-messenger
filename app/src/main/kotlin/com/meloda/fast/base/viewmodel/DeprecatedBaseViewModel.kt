package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.network.*
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.notNull
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Deprecated("rewrite")
abstract class DeprecatedBaseViewModel : ViewModel() {

    private val tasksEventChannel = Channel<VkEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch { onException(throwable) }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(exceptionHandler, block = block)
    }

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

    protected suspend fun <T> sendRequest(
        request: suspend () -> ApiAnswer<T>,
        onResponse: ResponseHandler<T>? = null,
        onError: ErrorHandler? = null,
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onAnyResult: (suspend () -> Unit)? = null,
        coroutineContext: CoroutineContext = Dispatchers.IO
    ): Job {
        val job = viewModelScope.launch(coroutineContext) {
            onStart?.invoke()

            when (val response = request.invoke()) {
                is ApiAnswer.Error -> {
                    onError?.handleError(response.error) ?: checkErrors(response.error)
                    onAnyResult?.invoke()
                }

                is ApiAnswer.Success -> {
                    onResponse?.handleResponse(response.data)
                    onAnyResult?.invoke()
                }
            }
        }

        job.invokeOnCompletion {
            viewModelScope.launch {
                onEnd?.invoke()
            }
        }

        return job
    }

    // TODO: 05.04.2023, Danil Nikolaev: переписать Conversations Screen на новую архитектуру, пока что оставить View

    protected fun <T> makeJob(
        job: suspend () -> ApiAnswer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null,
        onAnyResult: (suspend () -> Unit)? = null,
    ): Job = viewModelScope.launch {
        onStart?.invoke()
        when (val response = job()) {
            is ApiAnswer.Success -> {
                onAnswer(response.data)
                onAnyResult?.invoke()
            }

            is ApiAnswer.Error -> {
                onError?.invoke(response.error) ?: checkErrors(response.error)
                onAnyResult?.invoke()
            }
        }
    }.also {
        it.invokeOnCompletion {
            viewModelScope.launch {
                onEnd?.invoke()
            }
        }
    }

    protected open suspend fun onException(throwable: Throwable) {
        checkErrors(throwable)
    }

    protected suspend fun <T : VkEvent> sendEvent(event: T) = tasksEventChannel.send(event)

    protected suspend fun checkErrors(throwable: Throwable) {
        when (throwable) {
            is TokenExpiredError -> sendEvent(TokenExpiredErrorEvent)
            is AuthorizationError -> sendEvent(AuthorizationErrorEvent)
            is UserBannedError -> {
                val banInfo = throwable.banInfo
                sendEvent(
                    UserBannedEvent(
                        memberName = banInfo.memberName,
                        message = banInfo.message,
                        restoreUrl = banInfo.restoreUrl,
                        accessToken = banInfo.accessToken
                    )
                )
            }

            is ValidationRequiredError -> {
                sendEvent(
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

            is CaptchaRequiredError -> sendEvent(
                CaptchaRequiredEvent(
                    sid = throwable.captchaSid,
                    image = throwable.captchaImg
                )
            )

            is ApiError -> sendEvent(
                if (throwable.errorMessage == null) {
                    UnknownErrorEvent
                } else {
                    ErrorTextEvent(errorText = requireNotNull(throwable.errorMessage))
                }
            )

            else -> sendEvent(
                if (throwable.message == null) {
                    UnknownErrorEvent
                } else {
                    ErrorTextEvent(requireNotNull(throwable.message))
                }
            )
        }
    }
}
