package com.meloda.fast.base.viewmodel

abstract class VkEvent
abstract class VkErrorEvent(open val errorText: String? = null) : VkEvent()
abstract class VkProgressEvent : VkEvent()

open class ErrorTextEvent(override val errorText: String) : VkErrorEvent()

object AuthorizationErrorEvent : VkErrorEvent()
data class CaptchaRequiredEvent(val sid: String, val image: String) : VkErrorEvent()
data class ValidationRequiredEvent(val sid: String) : VkErrorEvent()

object StartProgressEvent : VkProgressEvent()
object StopProgressEvent : VkProgressEvent()

interface VkEventCallback<in T : Any> {
    fun onEvent(event: T)
}