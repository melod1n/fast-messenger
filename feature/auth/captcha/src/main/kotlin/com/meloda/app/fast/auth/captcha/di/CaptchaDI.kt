package com.meloda.app.fast.auth.captcha.di

import com.meloda.app.fast.auth.captcha.CaptchaViewModel
import com.meloda.app.fast.auth.captcha.CaptchaViewModelImpl
import com.meloda.app.fast.auth.captcha.validation.CaptchaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val captchaModule = module {
    singleOf(::CaptchaValidator)
    viewModelOf(::CaptchaViewModelImpl) bind CaptchaViewModel::class
}
