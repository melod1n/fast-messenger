package com.meloda.fast.base.viewmodel

abstract class VkEvent
abstract class VkErrorEvent(
    val throwable: Throwable? = null,
    val error: String? = null
) : VkEvent()

data class ShowDialogInfoEvent(
    val title: String? = null,
    val message: String,
    val positiveBtn: String? = null,
    val negativeBtn: String? = null
) : VkEvent()


data class ErrorEvent(val errorText: String) : VkErrorEvent(null, errorText)

object IllegalTokenEvent : VkErrorEvent()

data class CaptchaEvent(val sid: String, val image: String) : VkEvent()

data class ValidationEvent(val sid: String) : VkEvent()

object StartProgressEvent : VkEvent()
object StopProgressEvent : VkEvent()

interface VkEventCallback<in T : Any> {
    fun onEvent(event: T)
}