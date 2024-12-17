package dev.meloda.fast.auth.captcha.di

import dev.meloda.fast.auth.captcha.CaptchaViewModel
import dev.meloda.fast.auth.captcha.CaptchaViewModelImpl
import dev.meloda.fast.auth.captcha.validation.CaptchaValidator
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val captchaModule = module {
    singleOf(::CaptchaValidator)
    viewModelOf(::CaptchaViewModelImpl) bind CaptchaViewModel::class
}
