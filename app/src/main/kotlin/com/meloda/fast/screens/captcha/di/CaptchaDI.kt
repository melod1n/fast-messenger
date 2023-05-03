package com.meloda.fast.screens.captcha.di

import com.meloda.fast.di.navigationModule
import com.meloda.fast.screens.captcha.presentation.CaptchaCoordinator
import com.meloda.fast.screens.captcha.presentation.CaptchaCoordinatorImpl
import com.meloda.fast.screens.captcha.presentation.CaptchaViewModelImpl
import com.meloda.fast.screens.captcha.screen.CaptchaScreen
import com.meloda.fast.screens.captcha.validation.CaptchaValidator
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.bind
import org.koin.dsl.module

val captchaModule = module {
    val moduleQualifier = named("captcha")

    includes(navigationModule)

    single(moduleQualifier) { screen().resultFlow }
    single { screen().getArguments() }

    single {
        CaptchaCoordinatorImpl(
            resultFlow = get(moduleQualifier),
            router = get()
        )
    } bind CaptchaCoordinator::class

    singleOf(::CaptchaValidator)
    viewModelOf(::CaptchaViewModelImpl)
}

private fun Scope.screen(): CaptchaScreen = get()

