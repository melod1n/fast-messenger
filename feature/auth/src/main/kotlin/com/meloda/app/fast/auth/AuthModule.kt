package com.meloda.app.fast.auth

import com.meloda.app.fast.auth.captcha.di.captchaModule
import com.meloda.app.fast.auth.validation.di.validationModule
import com.meloda.fast.auth.login.di.loginModule
import org.koin.dsl.module

val authModule = module {
    includes(
        loginModule,
        validationModule,
        captchaModule,
    )
}
