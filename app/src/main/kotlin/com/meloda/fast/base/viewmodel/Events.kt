package com.meloda.fast.base.viewmodel

abstract class VkEvent

object VkNoneEvent : VkEvent()

abstract class VkErrorEvent(open val errorText: String? = null) : VkEvent()
abstract class VkProgressEvent : VkEvent()

object UnknownErrorEvent : VkErrorEvent()
open class ErrorTextEvent(override val errorText: String) : VkErrorEvent()

object AuthorizationErrorEvent : VkErrorEvent()
object TokenExpiredErrorEvent : VkErrorEvent()
data class CaptchaRequiredEvent(val sid: String, val image: String) : VkErrorEvent()
data class ValidationRequiredEvent(val sid: String) : VkErrorEvent()
data class UserBannedEvent(
    val memberName: String, val message: String, val restoreUrl: String, val accessToken: String,
) : VkErrorEvent()

object StartProgressEvent : VkProgressEvent()
object StopProgressEvent : VkProgressEvent()

fun interface VkEventCallback<in T : Any> {
    fun onEvent(event: T)
}
