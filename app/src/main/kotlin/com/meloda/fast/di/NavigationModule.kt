package com.meloda.fast.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import com.meloda.fast.screens.captcha.screen.CaptchaScreen
import com.meloda.fast.screens.twofa.screen.TwoFaScreen
import org.koin.core.module.dsl.singleOf
import org.koin.core.scope.Scope
import org.koin.dsl.module

val navigationModule = module {
    single { Cicerone.create() }
    single { cicerone().router }
    single { cicerone().getNavigatorHolder() }

    singleOf(::CaptchaScreen)
    singleOf(::TwoFaScreen)
}

private fun Scope.cicerone(): Cicerone<Router> = get()
