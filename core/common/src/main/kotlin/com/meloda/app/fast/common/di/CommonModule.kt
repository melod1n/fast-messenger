package com.meloda.app.fast.common.di

import coil.ImageLoader
import org.koin.dsl.module

val commonModule = module {
    single {
        ImageLoader.Builder(get())
            .crossfade(true)
            .build()
    }
}
