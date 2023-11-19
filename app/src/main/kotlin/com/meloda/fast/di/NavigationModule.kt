package com.meloda.fast.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import org.koin.core.scope.Scope
import org.koin.dsl.module

val navigationModule = module {
    single { Cicerone.create() }
    single { cicerone().router }
    single { cicerone().getNavigatorHolder() }
}

private fun Scope.cicerone(): Cicerone<Router> = get()
