package com.meloda.app.fast.auth.validation.model

import com.meloda.app.fast.common.UiText

data class ValidationScreenState(
    val code: String?,
    val codeError: String?,
    val isSmsButtonVisible: Boolean,
    val delayTime: Int,
    val phoneMask: String,

    // TODO: 13/07/2024, Danil Nikolaev: check wtf is this
    val validationText: UiText,
) {

    companion object {
        val EMPTY = ValidationScreenState(
            code = null,
            codeError = null,
            isSmsButtonVisible = false,
            delayTime = 0,
            phoneMask = "",

            validationText = UiText.Simple("")
        )
    }
}
