package dev.meloda.fast.common.di

import coil.ImageLoader
import dev.meloda.fast.common.LongPollController
import dev.meloda.fast.common.LongPollControllerImpl
import dev.meloda.fast.common.provider.ResourceProvider
import dev.meloda.fast.common.provider.ResourceProviderImpl
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
