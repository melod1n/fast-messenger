package com.meloda.fast.base.viewmodel

import android.util.Log
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
                onError?.invoke(response.throwable)
            }
        }
    }.also { it.invokeOnCompletion { viewModelScope.launch { onEnd?.invoke() } } }

    protected suspend fun <T : VKEvent> sendEvent(event: T) = tasksEventChannel.send(event)

    private suspend fun checkErrors(throwable: Throwable) {
        if (throwable is ApiError) {
            when (throwable.errorCode) {
                VkErrorCodes.USER_AUTHORIZATION_FAILED -> {
                    sendEvent(IllegalTokenEvent)
                    return
                }
            }
        } else if (throwable is VKException) {
            when (throwable.error) {
                VkErrors.NEED_CAPTCHA -> {
                    throwable.captcha =
                        (throwable.json?.optString("captcha_sid")
                            ?: "") to (throwable.json?.optString("captcha_img") ?: "")
                    return
                }
                VkErrors.NEED_VALIDATION -> {
                    throwable.validationSid = throwable.json?.optString("validation_sid")
                    return
                }


            }
        }

        sendEvent(ShowDialogInfoEvent(null, Log.getStackTraceString(throwable)))
    }

}