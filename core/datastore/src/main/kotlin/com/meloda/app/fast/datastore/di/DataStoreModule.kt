package com.meloda.app.fast.datastore.di

import com.meloda.app.fast.datastore.UserSettings
import com.meloda.app.fast.datastore.UserSettingsImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataStoreModule = module {
    singleOf(::UserSettingsImpl) bind UserSettings::class
}
