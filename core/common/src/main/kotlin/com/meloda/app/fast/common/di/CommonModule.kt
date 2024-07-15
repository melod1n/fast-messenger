package com.meloda.app.fast.common.di

import coil.ImageLoader
import com.meloda.app.fast.common.LongPollController
import com.meloda.app.fast.common.LongPollControllerImpl
import com.meloda.app.fast.common.provider.ResourceProvider
import com.meloda.app.fast.common.provider.ResourceProviderImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val commonModule = module {
    single {
        ImageLoader.Builder(get())
            .crossfade(true)
            .build()
    }

    singleOf(::LongPollControllerImpl) bind LongPollController::class
    singleOf(::ResourceProviderImpl) bind ResourceProvider::class
}
