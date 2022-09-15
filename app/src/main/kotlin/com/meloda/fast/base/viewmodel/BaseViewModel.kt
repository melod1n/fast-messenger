package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.network.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@Suppress("MemberVisibilityCanBePrivate")
abstract class BaseViewModel : ViewModel() {

    protected val tasksEventChannel = Channel<VkEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    protected val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        viewModelScope.launch { onException(throwable) }
    }

    fun launch(block: suspend CoroutineScope.() -> Unit): Job {
        return viewModelScope.launch(exceptionHandler, block = block)
    }

    protected suspend fun <T> makeSuspendJob(
        job: suspend () -> ApiAnswer<T>, onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null,
        onAnyResult: (suspend () -> Unit)? = null
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

    protected fun <T> makeJob(
        job: suspend () -> ApiAnswer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null,
        onAnyResult: (suspend () -> Unit)? = null
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
            is AuthorizationError -> {
                sendEvent(AuthorizationErrorEvent)
            }
            is TokenExpiredError -> {
                sendEvent(TokenExpiredErrorEvent)
            }
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
                sendEvent(ValidationRequiredEvent(throwable.validationSid))
            }
            is CaptchaRequiredError -> {
                sendEvent(
                    CaptchaRequiredEvent(
                        sid = throwable.captchaSid, image = throwable.captchaImg
                    )
                )
            }
            is ApiError -> {
                sendEvent(
                    if (throwable.errorMessage == null) {
                        UnknownErrorEvent
                    } else {
                        ErrorTextEvent(errorText = requireNotNull(throwable.errorMessage))
                    }
                )
            }
            else -> {
                sendEvent(
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