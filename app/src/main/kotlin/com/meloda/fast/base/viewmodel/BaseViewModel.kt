package com.meloda.fast.base.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.VKException
import com.meloda.fast.api.network.Answer
import com.meloda.fast.api.network.VKErrors
import com.meloda.fast.util.Utils
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
            is Answer.Error -> onError?.invoke(response.throwable)
        }
    }.also { it.invokeOnCompletion { viewModelScope.launch { onEnd?.invoke() } } }

    protected suspend fun <T : VKEvent> sendEvent(event: T) = tasksEventChannel.send(event)

    protected suspend fun checkErrors(throwable: Throwable) {
        // TODO: 8/31/2021 check illegal token
        if (throwable is VKException) {
            when (throwable.error) {
                VKErrors.NEED_CAPTCHA -> {
                    throwable.captcha =
                        (throwable.json?.optString("captcha_sid")
                            ?: "") to (throwable.json?.optString("captcha_img") ?: "")
                    return
                }
                VKErrors.NEED_VALIDATION -> {
                    throwable.validationSid = throwable.json?.optString("validation_sid")
                    return
                }


            }
        }

        tasksEventChannel.send(ShowDialogInfoEvent(null, Log.getStackTraceString(throwable)))
    }

}