package com.meloda.fast.base.viewmodel

import com.meloda.fast.model.base.UiText

abstract class VkEvent

abstract class VkErrorEvent(open val errorText: String? = null) : VkEvent()

object UnknownErrorEvent : VkErrorEvent()
open class ErrorTextEvent(override val errorText: String) : VkErrorEvent()

object AuthorizationErrorEvent : VkErrorEvent()
object TokenExpiredErrorEvent : VkErrorEvent()
data class CaptchaRequiredEvent(val sid: String, val image: String) : VkErrorEvent()
data class ValidationRequiredEvent(
    val sid: String,
    val redirectUri: String,
    val phoneMask: String,
    val validationType: String,
    val canResendSms: Boolean,
    val codeError: UiText?
) : VkErrorEvent()

data class UserBannedEvent(
    val memberName: String, val message: String, val restoreUrl: String, val accessToken: String,
) : VkErrorEvent()

fun interface VkEventCallback<in T : Any> {
    fun onEvent(event: T)
}
