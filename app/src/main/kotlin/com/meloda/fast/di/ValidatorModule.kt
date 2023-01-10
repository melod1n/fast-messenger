package com.meloda.fast.di

import com.meloda.fast.screens.login.validation.LoginValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ValidatorModule {

    @Singleton
    @Provides
    fun provideLoginValidator(): LoginValidator = LoginValidator()

}
