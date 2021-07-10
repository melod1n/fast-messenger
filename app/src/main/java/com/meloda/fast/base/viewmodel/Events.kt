package com.meloda.fast.base.viewmodel

data class ShowDialogInfoEvent(
    val title: String? = null,
    val message: String,
    val positiveBtn: String? = null,
    val negativeBtn: String? = null
) : VKEvent()