package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKException
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.network.ApiAnswer
import com.meloda.fast.api.network.VkErrorCodes
import com.meloda.fast.api.network.VkErrors
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    var unknownErrorDefaultText: String = ""

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
        onError: (suspend (Throwable) -> Unit)? = null
    ): ApiAnswer<T> {
        onStart?.invoke() ?: onStart()
        val response = job()

        when (response) {
            is ApiAnswer.Success -> onAnswer(response.data)
            is ApiAnswer.Error -> {
                checkErrors(response.throwable)
                onError?.invoke(response.throwable) ?: onError(response.throwable)
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
        onError: (suspend (Throwable) -> Unit)? = null
    ): Job = viewModelScope.launch {
        onStart?.invoke() ?: onStart()
        when (val response = job()) {
            is ApiAnswer.Success -> onAnswer(response.data)
            is ApiAnswer.Error -> {
                checkErrors(response.throwable)
                onError?.invoke(response.throwable) ?: onError(response.throwable)
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
        onError(throwable)
    }

    protected suspend fun onStart() {
        sendEvent(StartProgressEvent)
    }

    protected suspend fun onStop() {
        sendEvent(StopProgressEvent)
    }

    protected suspend fun onError(throwable: Throwable) {
        sendEvent(ErrorEvent(throwable.message ?: unknownErrorDefaultText))
    }

    protected suspend fun <T : VkEvent> sendEvent(event: T) = tasksEventChannel.send(event)

    private suspend fun checkErrors(throwable: Throwable) {
        when (throwable) {
            is ApiError -> {
                when (throwable.errorCode) {
                    VkErrorCodes.USER_AUTHORIZATION_FAILED -> {
                        sendEvent(IllegalTokenEvent)
                    }
                }
            }
            is VKException -> {
                when (throwable.error) {
                    VkErrors.NEED_CAPTCHA -> {
                        val json = throwable.json ?: return
                        sendEvent(
                            CaptchaEvent(
                                sid = json.optString("captcha_sid"),
                                image = json.optString("captcha_img")
                            )
                        )
                    }
                    VkErrors.NEED_VALIDATION -> {
                        val json = throwable.json ?: return
                        sendEvent(ValidationEvent(sid = json.optString("validation_sid")))
                    }
                }
            }
        }
    }

}