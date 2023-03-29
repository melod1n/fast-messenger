package com.meloda.fast.screens.captcha.screen

sealed class CaptchaResult {
    object Cancelled : CaptchaResult()
    data class Success(val code: String) : CaptchaResult()
}
