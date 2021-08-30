package com.meloda.fast.base.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meloda.fast.api.Answer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseVM : ViewModel() {

    protected val tasksEventChannel = Channel<VKEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    protected fun <T> makeJob(
        job: suspend () -> Answer<T>,
        onAnswer: suspend (T) -> Unit = {},
        onStart: (suspend () -> Unit)? = null,
        onEnd: (suspend () -> Unit)? = null,
        onError: (suspend (String) -> Unit)? = null
    ) = viewModelScope.launch {
        onStart?.invoke()
        when (val response = job()) {
            is Answer.Success -> onAnswer(response.data)
            is Answer.Error -> onError?.invoke(response.errorString) ?: tasksEventChannel.send(
                ShowDialogInfoEvent(null, response.errorString)
            )
        }
    }.also { it.invokeOnCompletion { viewModelScope.launch { onEnd?.invoke() } } }

    protected suspend fun <T : VKEvent> sendEvent(event: T) = tasksEventChannel.send(event)

}