package com.meloda.fast.screens.twofa.screen

sealed class TwoFaResult {
    object Cancelled : TwoFaResult()
    data class Success(val sid: String, val code: String) : TwoFaResult()
}
