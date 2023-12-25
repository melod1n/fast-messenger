package com.meloda.fast.di

import coil.ImageLoader
import org.koin.dsl.module

val utilModule = module {
    single {
        ImageLoader.Builder(get())
            .crossfade(true)
            .build()
    }
}
