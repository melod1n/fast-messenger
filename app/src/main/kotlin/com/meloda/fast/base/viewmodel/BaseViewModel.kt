package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.base.ApiException
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.ext.isTrue
import com.meloda.fast.ext.notNull
import com.slack.eithernet.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

abstract class BaseViewModel : ViewModel() {

    private val eventsFlow = MutableSharedFlow<VkEvent>()
    protected val events: Flow<VkEvent> = eventsFlow.asSharedFlow()

    private suspend fun sendSingleEvent(event: VkEvent) {
        eventsFlow.emit(event)
    }

    protected suspend fun <T> sendRequestNotNull(
        onError: ErrorHandler? = null,
        request: suspend () -> ApiAnswer<T>
    ): T = sendRequest(onError, request).notNull()

    protected suspend fun <T> sendRequest(
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

    protected suspend fun <T: Any, E: Any> sendRequestNew(
        request: suspend () -> ApiResult<T, E>,
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

//                is ApiAnswer.Error -> {
//                    onError?.handleError(response.error) ?: checkErrors(response.error)
//                    onAnyResult?.invoke()
//                }
//
//                is ApiAnswer.Success -> {
//                    onResponse?.handleResponse(response.data)
//                    onAnyResult?.invoke()
//                }

                else -> {}
            }
        }

        job.invokeOnCompletion {
            viewModelScope.launch {
                onEnd?.invoke()
            }
        }

        return job
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

    private suspend fun checkErrors(throwable: Throwable) {
        when (throwable) {
//            is TokenExpiredError -> {
//                sendSingleEvent(TokenExpiredErrorEvent)
//            }
//
//            is AuthorizationError -> {
//                sendSingleEvent(AuthorizationErrorEvent)
//            }
//
//            is UserBannedError -> {
//                throwable.banInfo.let { banInfo ->
//                    sendSingleEvent(
//                        UserBannedEvent(
//                            memberName = banInfo.memberName,
//                            message = banInfo.message,
//                            restoreUrl = banInfo.restoreUrl,
//                            accessToken = banInfo.accessToken
//                        )
//                    )
//                }
//            }

            // TODO: 15/11/2023, Danil Nikolaev: catch captcha everywhere
            // TODO: 30/12/2023, Danil Nikolaev: implement
//            is CaptchaRequiredError -> {
//                sendSingleEvent(
//                    CaptchaRequiredEvent(
//                        sid = throwable.captchaSid,
//                        image = throwable.captchaImg
//                    )
//                )
//            }

            is ApiException -> {
                sendSingleEvent(
                    ErrorTextEvent(errorText = requireNotNull(throwable.message))
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
