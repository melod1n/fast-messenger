package com.meloda.fast.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import com.meloda.fast.screens.captcha.screen.CaptchaScreen
import com.meloda.fast.screens.twofa.screen.TwoFaScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.kodein.di.*
import org.koin.dsl.module
import javax.inject.Singleton

private val cicerone = Cicerone.create()

@InstallIn(SingletonComponent::class)
@Module
object NavigationModule {

    @Provides
    @Singleton
    fun getCicerone(): Cicerone<Router> = cicerone

    @Provides
    @Singleton
    fun getRouter(cicerone: Cicerone<Router>) = cicerone.router

    @Provides
    @Singleton
    fun getNavigatorHolder(cicerone: Cicerone<Router>) = cicerone.getNavigatorHolder()
}

val navigationModule = module {
    single { cicerone }
    single { get<Cicerone<Router>>().router }
    single { get<Cicerone<Router>>().getNavigatorHolder() }

    single { CaptchaScreen() }
    single { TwoFaScreen() }
}
