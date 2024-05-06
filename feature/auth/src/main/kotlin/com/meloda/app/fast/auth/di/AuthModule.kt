package com.meloda.app.fast.auth.di

import com.meloda.app.fast.auth.AuthUseCase
import com.meloda.app.fast.auth.AuthUseCaseImpl
import com.meloda.app.fast.auth.OAuthUseCase
import com.meloda.app.fast.auth.OAuthUseCaseImpl
import com.meloda.app.fast.auth.screens.captcha.di.captchaModule
import com.meloda.app.fast.auth.screens.login.di.loginModule
import com.meloda.app.fast.auth.screens.logo.di.logoModule
import com.meloda.app.fast.auth.screens.twofa.di.twoFaModule
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val authModule = module {
    singleOf(::OAuthUseCaseImpl) bind OAuthUseCase::class
    singleOf(::AuthUseCaseImpl) bind AuthUseCase::class

    includes(
        logoModule,
        loginModule,
        twoFaModule,
        captchaModule,
    )
}
