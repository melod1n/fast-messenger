package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.network.*
import com.meloda.fast.ext.isTrue
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Deprecated("rewrite")
@Suppress("MemberVisibilityCanBePrivate")
abstract class DeprecatedBaseViewModel : ViewModel() {

    protected val tasksEventChannel = Channel<VkEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch { onException(throwable) }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(exceptionHandler, block = block)
    }

    protected var isFirstCreated = true

    suspend fun <T> ViewModel.sendRequest(
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

    protected suspend fun <T> makeSuspendJob(
        job: suspend () -> ApiAnswer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null,
        onAnyResult: (suspend () -> Unit)? = null,
    ): ApiAnswer<T> {
        onStart?.invoke() ?: onStart()
        val response = job()

        when (response) {
            is ApiAnswer.Success -> {
                onAnswer(response.data)
                onAnyResult?.invoke()
            }
            is ApiAnswer.Error -> {
                onError?.invoke(response.error) ?: checkErrors(response.error)
                onAnyResult?.invoke()
            }
        }

        onEnd?.invoke()

        return response
    }

    protected fun <T> makeJobWithErrorHandler(
        job: suspend () -> ApiAnswer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: ErrorHandler? = null,
        onAnyResult: (suspend () -> Unit)? = null,
    ): Job = viewModelScope.launch {
        onStart?.invoke() ?: onStart()
        when (val response = job()) {
            is ApiAnswer.Success -> {
                onAnswer(response.data)
                onAnyResult?.invoke()
            }
            is ApiAnswer.Error -> {
                if (!onError?.handleError(response.error).isTrue) {
                    checkErrors(response.error)
                }
                onAnyResult?.invoke()
            }
        }
    }.also {
        it.invokeOnCompletion {
            viewModelScope.launch {
                onEnd?.invoke() ?: onStop()
            }
        }
    }

    fun interface ErrorHandler {

        /**
         * @return true is error has been handled manually
         */
        suspend fun handleError(error: Throwable): Boolean
    }

    protected fun <T> makeJob(
        job: suspend () -> ApiAnswer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null,
        onAnyResult: (suspend () -> Unit)? = null,
    ): Job = viewModelScope.launch {
        onStart?.invoke() ?: onStart()
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
                onEnd?.invoke() ?: onStop()
            }
        }
    }

    protected open suspend fun onException(throwable: Throwable) {
        checkErrors(throwable)
    }

    protected suspend fun onStart() {
        sendEvent(StartProgressEvent)
    }

    protected suspend fun onStop() {
        sendEvent(StopProgressEvent)
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
            is ValidationRequiredError -> sendEvent(ValidationRequiredEvent(throwable.validationSid))
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
