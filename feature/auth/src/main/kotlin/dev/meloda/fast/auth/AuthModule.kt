package dev.meloda.fast.auth

import dev.meloda.fast.auth.captcha.di.captchaModule
import dev.meloda.fast.auth.validation.di.validationModule
import com.meloda.fast.auth.login.di.loginModule
import org.koin.dsl.module

val authModule = module {
    includes(
        loginModule,
        validationModule,
        captchaModule,
    )
}
