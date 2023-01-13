package com.meloda.fast.di

import com.meloda.fast.common.UpdateManager
import com.meloda.fast.common.UpdateManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindsModule {

    @Binds
    @Singleton
    abstract fun bindUpdateManager(updateManagerImpl: UpdateManagerImpl): UpdateManager

}
