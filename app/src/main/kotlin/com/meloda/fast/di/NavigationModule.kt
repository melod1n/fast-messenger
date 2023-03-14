package com.meloda.fast.di

import com.github.terrakok.cicerone.Cicerone
import com.github.terrakok.cicerone.Router
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object NavigationModule {
    @Provides
    @Singleton
    fun getCicerone(): Cicerone<Router> = Cicerone.create(Router())

    @Provides
    @Singleton
    fun getRouter(cicerone: Cicerone<Router>) = cicerone.router

    @Provides
    @Singleton
    fun getNavigatorHolder(cicerone: Cicerone<Router>) = cicerone.getNavigatorHolder()
}
