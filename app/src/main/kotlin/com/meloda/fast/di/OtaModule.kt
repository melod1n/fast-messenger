package com.meloda.fast.di

import com.meloda.fast.common.UpdateManager
import com.meloda.fast.common.UpdateManagerImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val otaModule = module {
    singleOf(::UpdateManagerImpl) { bind<UpdateManager>() }
}
