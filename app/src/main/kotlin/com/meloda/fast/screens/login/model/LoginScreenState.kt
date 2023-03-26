package com.meloda.fast.screens.login.model

data class LoginScreenState(
    val login: String,
    val password: String,
    val captchaSid: String?,
    val captchaCode: String?,
    val captchaImage: String?,
    val validationSid: String?,
    val validationCode: String?,
    val error: String?,
) {

    companion object {
        val EMPTY = LoginScreenState(
            login = "",
            password = "",
            captchaSid = null,
            captchaCode = null,
            captchaImage = null,
            validationSid = null,
            validationCode = null,
            error = null
        )
    }
}
