package com.meloda.fast.base.viewmodel

data class ShowDialogInfoEvent(
    val title: String? = null,
    val message: String,
    val positiveBtn: String? = null,
    val negativeBtn: String? = null
) : VKEvent()

data class ErrorEvent(val errorText: String) : VKEvent()

object IllegalTokenEvent : VKEvent()

object StartProgressEvent : VKEvent()
object StopProgressEvent : VKEvent()

abstract class VKEvent