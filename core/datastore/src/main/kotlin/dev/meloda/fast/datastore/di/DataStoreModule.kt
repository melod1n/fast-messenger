package dev.meloda.fast.datastore.di

import dev.meloda.fast.datastore.UserSettings
import dev.meloda.fast.datastore.UserSettingsImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataStoreModule = module {
    singleOf(::UserSettingsImpl) bind UserSettings::class
}
