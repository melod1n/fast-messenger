package com.meloda.fast.screens.captcha.di

import com.meloda.fast.di.navigationModule
import com.meloda.fast.screens.captcha.CaptchaViewModelImpl
import com.meloda.fast.screens.captcha.screen.CaptchaScreen
import com.meloda.fast.screens.captcha.validation.CaptchaValidator
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val captchaModule = module {
    includes(navigationModule)


    single { CaptchaValidator() }
    single(named("captchaResultFlow")) { get<CaptchaScreen>().resultFlow }

    viewModel {
        CaptchaViewModelImpl(
            resultFlow = get(named("captchaResultFlow")),
            router = get(),
            validator = get()
        )
    }
}

