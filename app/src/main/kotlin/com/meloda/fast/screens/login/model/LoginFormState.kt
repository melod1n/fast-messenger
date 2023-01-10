package com.meloda.fast.screens.login.model

data class LoginFormState(
    val login: String,
    val password: String,
    val captchaSid: String?,
    val captchaCode: String,
    val captchaImage: String?,
    val validationSid: String?,
    val validationCode: String,
    val error: String?,
) {

    companion object {
        val EMPTY = LoginFormState(
            login = "",
            password = "",
            captchaSid = null,
            captchaCode = "",
            captchaImage = null,
            validationSid = null,
            validationCode = "",
            error = null
        )
    }
}
