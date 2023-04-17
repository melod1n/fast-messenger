package com.meloda.fast.di

import com.meloda.fast.common.UpdateManager
import com.meloda.fast.common.UpdateManagerImpl
import com.meloda.fast.data.ota.OtaApi
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val otaModule = module {
    single { api(OtaApi::class.java) }
    singleOf(::UpdateManagerImpl) { bind<UpdateManager>() }
}
