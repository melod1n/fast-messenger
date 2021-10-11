package com.meloda.fast.base.viewmodel

data class ShowDialogInfoEvent(
    val title: String? = null,
    val message: String,
    val positiveBtn: String? = null,
    val negativeBtn: String? = null
) : VkEvent()

data class ErrorEvent(val errorText: String) : VkEvent()

object IllegalTokenEvent : VkEvent()
data class CaptchaEvent(val sid: String, val image: String) : VkEvent()
data class ValidationEvent(val sid: String) : VkEvent()

object StartProgressEvent : VkEvent()
object StopProgressEvent : VkEvent()

abstract class VkEvent