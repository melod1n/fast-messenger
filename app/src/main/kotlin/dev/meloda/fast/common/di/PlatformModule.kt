package dev.meloda.fast.common.di

import dev.meloda.fast.photoviewer.PlatformManager
import dev.meloda.fast.photoviewer.PlatformManagerImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val platformModule = module {
    factoryOf(::PlatformManagerImpl) bind PlatformManager::class
}
