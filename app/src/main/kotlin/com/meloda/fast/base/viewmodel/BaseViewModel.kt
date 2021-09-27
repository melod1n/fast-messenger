package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKException
import com.meloda.fast.api.base.ApiError
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VkErrorCodes
import com.meloda.fast.api.network.VkErrors
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    var unknownErrorDefaultText: String = ""

    protected val tasksEventChannel = Channel<VKEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    protected fun <T> makeJob(
        job: suspend () -> Answer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (Throwable) -> Unit)? = null
    ) = viewModelScope.launch {
        onStart?.invoke()
        when (val response = job()) {
            is Answer.Success -> onAnswer(response.data)
            is Answer.Error -> {
                checkErrors(response.throwable)
                onError?.invoke(response.throwable) ?: sendEvent(
                    ErrorEvent(
                        response.throwable.message
                            ?: unknownErrorDefaultText
                    )
                )
            }
        }
    }.also { it.invokeOnCompletion { viewModelScope.launch { onEnd?.invoke() } } }

    protected suspend fun <T : VKEvent> sendEvent(event: T) = tasksEventChannel.send(event)

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