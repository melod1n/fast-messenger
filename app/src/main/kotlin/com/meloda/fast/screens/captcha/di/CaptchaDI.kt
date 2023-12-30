package com.meloda.fast.screens.captcha.di

import com.meloda.fast.screens.captcha.CaptchaViewModelImpl
import com.meloda.fast.screens.captcha.validation.CaptchaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val captchaModule = module {
    singleOf(::CaptchaValidator)
    viewModelOf(::CaptchaViewModelImpl)
}
